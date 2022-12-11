package com.heeyjinny.seoulpubliclibraries

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.heeyjinny.seoulpubliclibraries.data.Library
import com.heeyjinny.seoulpubliclibraries.databinding.ActivityMapsBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

/**  서울 공공도서관 앱 개발하기  **/

//1
//구글 지도 API키 추가
//레트로핏, JSON컨버터 라이브러리 추가
//build.gradle(:app)

//2
//위치권한 및 인터넷권한 추가
//AndroidManifest.xml

//3
//사용할 JSON 샘플데이터를 사용하여
//코틀린 데이터 클래스 생성
//클래스의 개수가 여러 개일 경우 관리를 위해
//기본 패키지 밑에 data패키지 생성
//패키지 우클릭 - New - Package - data패키지 생성

//data패키지 폴더 우클릭 - New - Kotlin data class File from Json 클릭
//빈 여백에 샘플 데이터 붙여넣기, Format으로 정렬 후
//Class Name은 Library 입력

//4
//Open API 사용을 위해
//기본정보를 담아두는 클래스와
//클래스 안에 레트로핏에서 사용할 인터페이스 생성

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }//onCreate

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //8
        //loadLibraries()호출
        loadLibraries()

        //9-2
        //마커에 tag단 것을 이용해
        //마커를 클릭했을 때 홈페이지 주소를 웹브라우저로 생성
        mMap.setOnMarkerClickListener {

            //9-3
            //마커의 tag가 null값이 아니라면
            if (it.tag != null){

                //9-4
                //마커의 tag를 String으로 형변환하고
                //tag가 http로 시작하지 않으면
                //http://문자열을 앞에 추가
                var url = it.tag as String
                if (!url.startsWith("http")){
                    url = "http://${url}"
                }
                //9-5
                //완성된 url을 intent로 생성한 후
                //액티비티 호출
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
            true
        }

    }//onMapReady

    //5
    //정의한 인터페이스를 정의하고 데이터 불러오는 코드 작성
    //loadLibraries() 메서드 생성
    fun loadLibraries(){

        //5-1
        //도메인 주소와 JSON컨버터를 설정하여
        //레트로핏 생성
        val retrofit = Retrofit.Builder()
            .baseUrl(SeoulOpenApi.DOMAIN)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        //5-2
        //레트로핏을 사용해 정의해두었던 인터페이스를
        //실행가능한 서비스 객체로 변환
        val seoulOpenService = retrofit.create(SeoulOpenService::class.java)

        //5-3
        //인터페이스에 정의된 getLibrary()메서드에 파라미터로
        //SeoulOpenApi의 API_KEY를 입력하고
        //enqueue()메서드를 호출하여 서버에 요청
        seoulOpenService.getLibrary(SeoulOpenApi.API_KEY).enqueue(object: Callback<Library>{

            //5-4
            //인터페이스의 코드 2개 자동 생성
            override fun onResponse(call: Call<Library>, response: Response<Library>) {

                //5-5
                //서버 요청이 정상적으로 되었다면
                //지도에 마커를 표시하는 메서드 호출(마커 표시 메서드 아래에 생성필요...)
                showLibraries(response.body() as Library)

            }

            override fun onFailure(call: Call<Library>, t: Throwable) {

                //5-6
                //서버 요청이 실패했을 경우 토스트메시지 생성
                Toast.makeText(this@MapsActivity, "서버요청 실패", Toast.LENGTH_SHORT).show()
            }

        })

    }//loadLibraries

    //6
    //지도에 도서관 마커 표시하는 메서드생성
    fun showLibraries(libraries: Library){

        //7
        //마커가 지도에 표기되지만 지도를 보여주는 카메라는 시드니를 가리킴
        //카메라 위치조정 필요
        //마커 전체의 영역으로 먼저 구해
        //마커의 영역만큼 보여주는 코드 작성
        //마커의 영역으로 저장하는 LatLngBounds.Builder생성
        val latLngBounds = LatLngBounds.Builder()

        //6-1
        //파라미터로 전달된 libraries의
        //SeoulPublicLibraryInfo.row에 도서관 목록이 있음
        //반복문으로 하나씩 꺼내어 미커 생성하여 추가
        for (lib in libraries.SeoulPublicLibraryInfo.row){

            //6-2
            //마커좌표 생성
            val position = LatLng(lib.XCNTS.toDouble(), lib.YDNTS.toDouble())

            //6-3
            //좌표와 도서관 이름으로 마커 생성
            val marker = MarkerOptions().position(position).title(lib.LBRRY_NAME)

            //6-4
            //마커를 지도에 추가
            mMap.addMarker(marker)

            //9
            //도서관 이름 클릭 시 홈페이지로 이동하기
            //도서관 홈페이지 URL검사 후
            //홈페이지를 웹 프라우저에 띄우는 코드 작성
            //9-1
            //6-4코드 수정
            //마커에 tag정보 추가
            //지도에 마커를 추가하고 그 마커 tag값에 홈페이지 주소 저장
            var obj = mMap.addMarker(marker)
            obj?.tag = lib.HMPG_URL

            //7-1
            //지도에 마커 추가 후 latLngBounds에도 마커 추가
            latLngBounds.include(marker.position)

        }

        //7-2
        //앞에서 저장해둔 마커의 영역 구하기
        //padding변수는 마커의 영역에 여백을 얼마나 줄 것인지 정함
        val bounds = latLngBounds.build()
        val padding = 0

        //7-3
        //카메라 업데이트
        //bounds와 padding설정하여 카메라 업데이트
        val updated = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        //7-4
        //업데이트된 카메라 지도에 반영
        mMap.moveCamera(updated)

    }//showLibraries

}//MapsActivity