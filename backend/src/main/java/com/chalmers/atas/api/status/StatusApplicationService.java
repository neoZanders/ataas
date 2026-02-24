package com.chalmers.atas.api.status;

import com.chalmers.atas.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusApplicationService {

    public Result<Void> getStatus() {
        return Result.ok();
    }
}
