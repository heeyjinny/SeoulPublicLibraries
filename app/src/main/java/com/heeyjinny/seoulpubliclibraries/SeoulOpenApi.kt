package com.heeyjinny.seoulpubliclibraries

import com.heeyjinny.seoulpubliclibraries.data.Library
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

//1
//Open API 사용을 위해
//기본정보를 담아두는 클래스와
//클래스 안에 레트로핏에서 사용할 인터페이스 생성
class SeoulOpenApi {

    //2
    //companion object{}를 만들어
    //도메인 주소와 API키 저장해놓은 변수 2개 생성
    //companion object: 블록안에 변수 생성 시 '클래스명.변수명' 형식으로
    //바로 사용할 수 있음
    companion object{
        val DOMAIN = BuildConfig.LBRRY_DOMAIN
        val API_KEY = BuildConfig.LBRRY_API_KEY
    }

}//SeoulOpenApi

//3
//레트로핏에서 사용할 인터페이스 SeoulOpenService생성
interface SeoulOpenService{

    //3-1
    //도서관 데이터를 가져오는 getLibrary()메서드 정의
    //@GET 애노테이션 사용해 호출할 주소 지정
    //레트로핏에서 사용할 때 @GET에 입력된 주소와
    //정의해둔 DOMAIN주소를 조합해 사용할 것임

    //getLibrary()메서드의 파라미터로 사용된 key는
    //SeoulOpenApi클래스에 정의한 API_KEY를
    //레트로핏을 실행하는 코드에서 넘겨받은 후 주소와 결합하고
    //반환 값은 Call<JSON변환된 클래스>

    //도서관의 개수가 총 120개 이므로 한 페이지에 모두 불러오기 위해
    //주소 끝 부분에 페이지1/가져올개수200 입력

//    @GET("/json/SeoulPublicLibraryInfo/1/200/")
//    fun getLibrary(key: String): Call<Library>

    //위 코드 수정하기...
    //@Path애노테이션 사용
    //메서드의 파라미터로 넘어온 값을
    //@GET에 정의된 주소에 동적으로 삽일할 수 있음...

    //메서드의 파라미터 변수 앞에 @Path 애노테이션으로
    //@GET 주소에 매핑할 이름 작성하고
    //@GET문자열에 {매핑할 이름}형식으로 삽입하면
    //메서드가 호출되는 순간 매핑할 이름이
    //정의된 파라미터의 값으로 대체된 후 사용됨

    @GET("{api_kry}/json/SeoulPublicLibraryInfo/1/200/")
    fun getLibrary(@Path("api_key") key: String): Call<Library>

}

//4
//정의한 인터페이스를 정의하고 데이터 불러오는 코드 작성
//MapsActivity.kr