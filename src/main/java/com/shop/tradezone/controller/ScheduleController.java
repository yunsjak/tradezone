
package com.shop.tradezone.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/baseball")
public class ScheduleController {

	@GetMapping("/day")
	public String index() {
		return "baseballday";
	}

	@GetMapping("/data")
	@ResponseBody
	public ResponseEntity<String> getSchedule(@RequestParam(value = "month", defaultValue = "202509") String month) {
		String url = String.format(
				"https://sports.daum.net/prx/hermes/api/game/schedule.json?page=1&leagueCode=kbo&seasonKey=2025&fromDate=%s01&toDate=%s31",
				month, month);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "Mozilla/5.0");

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

		return response;
	}

//-----------
	@GetMapping("/today")
	@ResponseBody
	public ResponseEntity<String> getTodaySchedule() {
		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		String url = String.format(
				"https://sports.daum.net/prx/hermes/api/game/schedule.json?page=1&leagueCode=kbo&seasonKey=2025&fromDate=%s&toDate=%s",
				today, today);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "Mozilla/5.0");

		HttpEntity<String> entity = new HttpEntity<>(headers);

		return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

//		String dummyJson = """
//				           {
//				              "date": "2025-09-04",
//				              "games": [
//				                {
//				                  "homeTeamName": "NC",
//				                  "awayTeamName": "두산",
//				                  "homeTeamImageUrl": "http://t1.daumcdn.net/media/img-section/sports13/logo/team/1/NC_300300.png",
//				                  "awayTeamImageUrl": "http://t1.daumcdn.net/media/img-section/sports13/logo/team/1/OB_300300.png",
//				                  "homeStartPitcher": "홍성민",
//				                  "awayStartPitcher": "김철수",
//				                  "startTime": "18:30",
//				                  "fieldName": "창원NC파크",
//				                  "gameStatus": "BEFORE",
//				                  "homeResult": null,
//				                  "awayResult": null
//				                },
//				                {
//				                  "homeTeamName": "KIA",
//				                  "awayTeamName": "SSG",
//				                  "homeTeamImageUrl": "http://t1.daumcdn.net/media/img-section/sports13/logo/team/1/HT_300300.png",
//				                  "awayTeamImageUrl": "http://t1.daumcdn.net/media/img-section/sports13/logo/team/1/SK_300300.png",
//				                  "homeStartPitcher": "박진우",
//				                  "awayStartPitcher": "이영하",
//				                  "startTime": "18:30",
//				"fieldName": "광주-기아 챔피언스필드",
//				                      "gameStatus": "BEFORE",
//				                      "homeResult": null,
//				                      "awayResult": null
//				                    },
//				                    {
//				                      "homeTeamName": "삼성",
//				                      "awayTeamName": "키움",
//				                      "homeTeamImageUrl": "http://t1.daumcdn.net/media/img-section/sports13/logo/team/1/SS_300300.png",
//				                      "awayTeamImageUrl": "http://t1.daumcdn.net/media/img-section/sports13/logo/team/1/WO_300300.png",
//				                      "homeStartPitcher": "김민수",
//				                      "awayStartPitcher": "조상우",
//				                      "startTime": "18:30",
//				                      "fieldName": "대구 삼성 라이온즈 파크",
//				                      "gameStatus": "BEFORE",
//				                      "homeResult": null,
//				                      "awayResult": null
//				                    }
//				                  ]
//				                }
//				               """;
//		return ResponseEntity.ok(dummyJson);
	}

}