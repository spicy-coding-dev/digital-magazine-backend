package com.digital.magazine.common.response;

import com.digital.magazine.common.enums.Role;

public record LoginApiResponse(String accessToken, Role userRole) {

}
