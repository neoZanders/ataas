package com.chalmers.atas.api.status;

import com.chalmers.atas.common.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/status")
public class StatusController {

    // @RequiredArgsConstructor automatically injects any private final
    // beans (spring components (anything with @Service or @Component + more)
    private final StatusApplicationService statusApplicationService;

    /*
    Any Http method mapping annotation (such as this one) will inherit the
    prefix defined in the class level request mapping annotation. It can
    take an input param (String) to build on that prefix if needed.
    */
    @GetMapping
    public HttpResponse<Void> getUserDetails() {
        return HttpResponse.fromResult(statusApplicationService.getStatus());
    }
}
