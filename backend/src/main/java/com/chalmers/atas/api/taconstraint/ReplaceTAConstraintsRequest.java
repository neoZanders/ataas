package com.chalmers.atas.api.taconstraint;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ReplaceTAConstraintsRequest {
    List<ReplaceTAConstraintRequest> requests;
}
