package com.example.picket.common.consts;

import java.util.Map;

public interface Const {

    String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$";

    //TODO : 추후 로그인 없이도 방문가능한 페이지의 경우 해당 화이트 리스트에 URL 추가
    Map<String, String[]> WHITE_LIST = Map.of(
            "GET", new String[]{
                    "/api/v*/shows",
                    "/api/v*/shows/*",
                    "/api/v*/shows/*/comments",
                    "/swagger-ui/*",
                    "/v3/api-docs/**"
            },
            "POST", new String[]{
                    "/api/v*/auth/signup/*",
                    "/api/v*/auth/signin",
                    "/swagger-ui/*",
                    "/v3/api-docs/**"
            },
            "PUT", new String[]{

            },
            "PATCH", new String[]{

            },
            "DELETE", new String[]{

            }
    );
}
