package com.chalmers.atas.external;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.config.TimeEditProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import okhttp3.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimeEditClient {

    private static final String BASE_URL = "https://api.timeedit.net/v1";
    private static final List<Integer> COURSE_ACTIVITY_IDS = List.of(22, 23, 24);
    private static final ZoneId TIME_EDIT_ZONE = ZoneId.of("Europe/Stockholm");
    private static final MediaType JSON = MediaType.parse("application/json");

    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;
    private final TimeEditProperties properties;

    private String accessToken;

    public Result<List<Pair<LocalDateTime, LocalDateTime>>> fetchCourseSessionStartAndEnds(
            String courseCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return ensureAuthenticated()
                .then(() -> fetchCourseOfferingId(courseCode, startDate, endDate))
                .flatMap(courseOfferingId -> fetchReservationsForActivities(courseOfferingId, startDate, endDate))
                .map(reservations -> reservations.stream()
                        .map(this::toStartEndPair)
                        .toList());
    }

    private Result<List<ReservationResponse>> fetchReservationsForActivities(
            int courseOfferingId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<ReservationResponse> allReservations = new ArrayList<>();

        for (int activityId : COURSE_ACTIVITY_IDS) {
            Result<List<ReservationResponse>> result =
                    fetchReservations(courseOfferingId, activityId, startDate, endDate);

            if (!result.isSuccess()) {
                return Result.error(result.getError());
            }

            allReservations.addAll(result.getData());
        }

        return Result.ok(allReservations);
    }

    private Result<List<ReservationResponse>> fetchReservations(
            int courseOfferingId,
            int activityId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("page", 1);
        requestBody.put("limit", 100);

        ObjectNode dateNode = requestBody.putObject("date");
        dateNode.put("startDate", toEpochSeconds(startDate));
        dateNode.put("endDate", toEpochSecondsExclusive(endDate));

        ArrayNode searchObjects = requestBody.putArray("searchObjects");

        ObjectNode courseOfferingNode = searchObjects.addObject();
        courseOfferingNode.put("typeId", 10);
        courseOfferingNode.put("objectId", courseOfferingId);

        ObjectNode activityNode = searchObjects.addObject();
        activityNode.put("typeId", 12);
        activityNode.put("objectId", activityId);

        Request request = authorizedJsonPost(
                buildUrl("reservations", "find"),
                requestBody
        );

        return ensureAuthAndExecute(request)
                .flatMap(this::parseReservationsResponse);
    }

    private Pair<LocalDateTime, LocalDateTime> toStartEndPair(ReservationResponse reservation) {
        return Pair.of(
                Instant.ofEpochSecond(reservation.begin())
                        .atZone(TIME_EDIT_ZONE)
                        .toLocalDateTime(),
                Instant.ofEpochSecond(reservation.end())
                        .atZone(TIME_EDIT_ZONE)
                        .toLocalDateTime()
        );
    }

    private Result<Void> ensureAuthenticated() {
        if (hasAccessToken()) {
            return Result.ok();
        }
        return authenticate();
    }

    private boolean hasAccessToken() {
        return accessToken != null && !accessToken.isBlank();
    }

    private Result<Void> authenticate() {
        Request request = new Request.Builder()
                .url(buildUrl("organizations", properties.organizationId(), "api-keys", "authenticate"))
                .post(RequestBody.create(new byte[0], null))
                .addHeader("Authorization", properties.apiKey())
                .addHeader("Accept", "application/json")
                .addHeader("X-Region", "EU_EES")
                .build();

        return execute(request).flatMap(responseBody -> {
            try {
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode tokenNode = root.get("token");

                if (tokenNode == null || tokenNode.isNull() || tokenNode.asText().isBlank()) {
                    return Result.error(ErrorCode.INTERNAL_SERVER_ERROR.toError(
                            "TimeEdit authentication response did not contain token"
                    ));
                }

                accessToken = tokenNode.asText();
                return Result.ok();
            } catch (IOException e) {
                return Result.error(ErrorCode.INTERNAL_SERVER_ERROR.toError(
                        "Failed to parse TimeEdit authentication response"
                ));
            }
        });
    }

    private Result<Integer> fetchCourseOfferingId(
            String courseCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("idFormat", "EXTERNAL");
        requestBody.put("typeId", "courseoffering");
        requestBody.put("includeReferenceFields", true);

        ObjectNode fieldsNode = requestBody.putObject("fields");
        ArrayNode exactSearchFields = fieldsNode.putArray("exactSearchFields");

        ObjectNode courseCodeField = exactSearchFields.addObject();
        courseCodeField.put("fieldId", "courseoffering.coursecode");
        courseCodeField.putArray("values").add(courseCode);

        Request request = authorizedJsonPost(
                buildUrl("objects", "find"),
                requestBody
        );

        return ensureAuthAndExecute(request).flatMap(responseBody -> {
            try {
                FindCourseOfferingResponse response = objectMapper.readValue(
                        responseBody,
                        FindCourseOfferingResponse.class
                );

                System.out.println("results=" + response.results());

                return Result.ofOptional(
                        response.results().stream()
                                .filter(offering -> overlaps(offering, startDate, endDate))
                                .map(CourseOfferingResponse::id)
                                .findFirst(),
                        ErrorCode.INTERNAL_SERVER_ERROR.toError(
                                "Could not find course offering matching provided code and dates in TimeEdit response"
                        )
                );
            } catch (IOException e) {
                return Result.error(ErrorCode.INTERNAL_SERVER_ERROR.toError(
                        "Failed to parse TimeEdit course offering response"
                ));
            }
        });
    }

    private boolean overlaps(
            CourseOfferingResponse offering,
            LocalDate requestedStart,
            LocalDate requestedEnd
    ) {
        LocalDate offeringStart = getDateField(offering, "courseoffering.start");
        LocalDate offeringEnd = getDateField(offering, "courseoffering.end");

        System.out.println("offeringId=" + offering.id());
        System.out.println("offeringStart=" + offeringStart + ", requestedStart=" + requestedStart);
        System.out.println("offeringEnd=" + (offeringEnd) + ", requestedEnd=" + requestedEnd);

        if (offeringStart == null || offeringEnd == null) {
            return false;
        }

        System.out.println("overlaps=" + (!offeringEnd.isBefore(requestedStart) && !offeringStart.isAfter(requestedEnd)));

        return !offeringEnd.isBefore(requestedStart) && !offeringStart.isAfter(requestedEnd);
    }

    private LocalDate getDateField(CourseOfferingResponse offering, String fieldId) {
        return offering.fields().stream()
                .filter(field -> fieldId.equals(field.fieldId()))
                .findFirst()
                .flatMap(field -> field.values().stream().findFirst())
                .map(LocalDate::parse)
                .orElse(null);
    }

    private Result<List<ReservationResponse>> parseReservationsResponse(String responseBody) {
        try {
            FindReservationsResponse response = objectMapper.readValue(
                    responseBody,
                    FindReservationsResponse.class
            );

            return Result.ok(response.results() != null ? response.results() : List.of());
        } catch (IOException e) {
            return Result.error(ErrorCode.INTERNAL_SERVER_ERROR.toError(
                    "Failed to parse TimeEdit reservations response"
            ));
        }
    }

    private Result<String> execute(Request request) {
        try (Response response = okHttpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                return Result.error(
                        ErrorCode.fromHttpStatus(HttpStatus.valueOf(response.code()))
                                .toError("TimeEdit error " + body)
                );
            }

            return Result.ok(body);
        } catch (IOException e) {
            return Result.error(ErrorCode.REQUEST_TIMED_OUT.toError());
        }
    }

    private Result<String> ensureAuthAndExecute(Request request) {
        return execute(request).orGetIfError(
                ErrorCode.FORBIDDEN.toError(),
                () -> authenticate().then(() -> execute(request))
        );
    }

    private Request authorizedJsonPost(HttpUrl url, JsonNode requestBody) {
        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody.toString(), JSON))
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
    }

    private HttpUrl buildUrl(String... pathSegments) {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(BASE_URL)).newBuilder();
        for (String pathSegment : pathSegments) {
            builder.addPathSegment(pathSegment);
        }
        return builder.build();
    }

    private long toEpochSeconds(LocalDate date) {
        return date.atStartOfDay(TIME_EDIT_ZONE)
                .toInstant().getEpochSecond();
    }

    private long toEpochSecondsExclusive(LocalDate date) {
        return date.plusDays(1)
                .atStartOfDay(TIME_EDIT_ZONE)
                .toInstant().getEpochSecond();
    }
}