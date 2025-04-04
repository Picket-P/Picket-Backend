package com.example.picket.common.consts;

import java.util.Map;

public interface Const {

    String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$";

    //TODO : 추후 로그인 없이도 방문가능한 페이지의 경우 해당 화이트 리스트에 URL 추가
    Map<String, String[]> WHITE_LIST = Map.of(
            "GET", new String[]{
                    "/api/v1/shows/*/comments"
            },
            "POST", new String[]{
                    "/api/v1/auth/signup/*",
                    "/api/v1/auth/signin",
            },
            "PUT", new String[]{

            },
            "PATCH", new String[]{

            },
            "DELETE", new String[]{

            }
    );
}
