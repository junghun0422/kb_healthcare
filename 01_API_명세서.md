# kb_healthcare API 명세서

이 문서는 `kb_healthcare` 프로젝트의 주요 API에 대한 명세를 정리합니다.

## 1. 인증 서버 API (`authserver`)

`authserver`는 사용자 인증, 로그인 및 토큰 관리를 담당합니다.

### 1.1 회원가입

- **Endpoint**: `POST /v1/user/join`
- **설명**: 새로운 사용자를 시스템에 등록합니다.
- **Request Body**:
    ```json
    {
      "email": "user@example.com",
      "password": "password123",
      "name": "홍길동",
      "nickName": "길동이"
    }
    ```
- **Response**:
    - **성공 (200 OK)**:
        ```json
        {
          "resultCode": "COMMON-0000",
          "resultMessage": "요청에 성공했습니다.",
          "data": null
        }
        ```
    - **실패 (4xx/5xx)**:
        ```json
        {
          "resultCode": "ERROR_CODE",
          "resultMessage": "Error message",
          "data": null
        }
        ```

### 1.2 로그인

- **Endpoint**: `POST /v1/user/login`
- **설명**: 사용자 이메일과 비밀번호로 로그인하고, JWT 토큰(Access Token, Refresh Token)을 발급받습니다.
- **Request Body**:
    ```json
    {
      "email": "user@example.com",
      "password": "password123"
    }
    ```
- **Response**:
    - **성공 (200 OK)**:
        ```json
        {
          "resultCode": "COMMON-0000",
          "resultMessage": "요청에 성공했습니다.",
          "data": {
            "grantType": "Bearer",
            "accessToken": "ey...",
            "refreshToken": "ey...",
            "accessTokenExpiresIn": 3600
          }
        }
        ```

### 1.3 토큰 갱신

- **Endpoint**: `POST /v1/user/refresh`
- **설명**: 만료된 Access Token을 Refresh Token을 사용하여 갱신합니다.
- **Request Header**:
    - `Authorization`: `Bearer {Refresh Token}`
- **Response**:
    - **성공 (200 OK)**:
        ```json
        {
          "resultCode": "COMMON-0000",
          "resultMessage": "요청에 성공했습니다.",
          "data": {
            "grantType": "Bearer",
            "accessToken": "ey...",
            "refreshToken": "ey...",
            "accessTokenExpiresIn": 3600
          }
        }
        ```

---

## 2. 건강 기록 API (`record`)

`record` 서버는 사용자의 건강 기록(예: 걸음수)을 관리합니다. 모든 API는 `authserver`에서 발급한 Access Token을 필요로 합니다.

**공통 Request Header**: `Authorization`: `Bearer {Access Token}`

### 2.1 걸음수 기록

- **Endpoint**: `POST /v1/record`
- **설명**: 특정 날짜의 걸음수를 기록합니다.
- **Request Body**:
    ```json
    {
        "recordkey" : "7836887b-b12a-440f-af0f-851546504b13",
        "data": {
            "memo": "",
            "entries": [
                {
                    "steps": "287.6726411513615",
                    "period": {
                        "to": "2024-11-14T23:10:00+0000",
                        "from": "2024-11-14T23:00:00+0000"
                    },
                    "distance": {
                        "value": 0.2301381129210892,
                        "unit": "km"
                    },
                    "calories": {
                        "value": 0,
                        "unit": "kcal"
                    }
                },
                {
                    "steps": "1031.128197585273",
                    "period": {
                        "to": "2024-11-14T23:20:00+0000",
                        "from": "2024-11-14T23:10:00+0000"
                    },
                    "distance": {
                        "value": 0.8249025580682184,
                        "unit": "km"
                    },
                    "calories": {
                        "value": 0,
                        "unit": "kcal"
                    }
                }
            ],
            "source": {
                "product": {
                    "name": "iPhone",
                    "vender": "Apple inc."
                },
                "type": "",
                "mode": 10,
                "name": "Health Kit"
            }
        },
        "lastUpdate": "2024-12-16 14:40:00 +0000",
        "type": "steps"
    }
    ```
- **Response**:
    - **성공 (200 OK)**:
        ```json
        {
          "resultCode": "COMMON-0000",
          "resultMessage": "요청에 성공했습니다.",
          "data": null
        }
        ```

**공통 Request Header**: `Authorization`: `Bearer {Access Token}`

### 2.2 건강기록 조회
- **Endpoint**: `GET /v1/record`
- **설명**: 사용자의 건강기록을 조회한다.
- **Response**:
- **성공 (200 OK)**:
    ```json
    {
      "resultCode": "COMMON-0000",
      "resultMessage": "요청에 성공했습니다.",
      "data": [
        {
          "recordkey": "3b87c9a4-f983-4168-8f27-85436447bb57",
          "data" : {
            "memo": null,
            "entries" : [
              {
                "period": {
                  "from": "2024-11-15T00:00:00",
                  "to": "2024-11-15T00:00:00"
                },
                "distance": {
                  "unit": "km",
                  "value": 0.24368
                },
                "calories": {
                  "unit": "kcal",
                  "value": 9.75999
                },
                "steps": 312.0
              },
              {
                "period": {
                  "from": "2024-11-15T00:00:00",
                  "to": "2024-11-15T00:10:00"
                },
                "distance": {
                  "unit": "km",
                  "value": 0.09212
                },
                "calories": {
                  "unit": "kcal",
                  "value": 3.97
                },
                "steps": 120.0
              } 
            ],
            "source": {
              "mode": 9,
              "product": {
                "name": "Android",
                "vender": "Samsung"
              },
              "name": "SamsungHealth",
              "type": ""
            },
            "lastUpdate": "2024-12-16T14:30:00",
            "type": ""      
          }     
        },
        {
          "recordkey": "7836887b-b12a-440f-af0f-851546504b13",
          "data" : {
            "memo": null,
            "entries": [
              {
                "period": {
                  "from": "2024-11-15T00:00:00",
                  "to": "2024-11-15T00:10:00"
                },
                "distance": {
                  "unit": "km",
                  "value": 0.04223
                },
                "calories": {
                  "unit": "kcal",
                  "value": 2.03
                },
                "steps": 54.0
               }
            ],
            "source" : {
              "mode": 9,
              "product": {
                "name": "Android",
                "vender": "Samsung"
              },
              "name": "SamsungHealth",
              "type": ""
            } 
          },
          "lastUpdate": "2024-12-16T14:40:00",
          "type": "" 
        }
      ] 
    }
    ```