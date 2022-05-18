package ru.usb.springbootcbrfmpr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.usb.dailyinfo.wsdl.GetCursOnDateResponse;
import ru.usb.dailyinfo.wsdl.GetLatestDateTimeResponse;
import ru.usb.springbootcbrfmpr.Service.SoapClient;


@SpringBootApplication
@EnableScheduling
public class SpringBootCbrfMprApplication {

	static String getLatestDateTimeXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://web.cbr.ru/\">\n" +
			"   <soapenv:Header/>\n" +
			"   <soapenv:Body>\n" +
			"      <web:GetLatestDateTime/>\n" +
			"   </soapenv:Body>\n" +
			"</soapenv:Envelope>";

	static String getCursOnDateXml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://web.cbr.ru/\">\n" +
			"   <soapenv:Header/>\n" +
			"   <soapenv:Body>\n" +
			"      <web:GetCursOnDate>\n" +
			"         <web:On_date>2022-05-18T00:00:00</web:On_date>\n" +
			"      </web:GetCursOnDate>\n" +
			"   </soapenv:Body>\n" +
			"</soapenv:Envelope>";

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SpringBootCbrfMprApplication.class, args);
		SoapClient soapClient = new SoapClient();


		//soapClient.sendSoap(getLatestDateTimeXml, GetLatestDateTimeResponse.class);
		soapClient.sendSoap(getCursOnDateXml, GetCursOnDateResponse.class);

	}

}
