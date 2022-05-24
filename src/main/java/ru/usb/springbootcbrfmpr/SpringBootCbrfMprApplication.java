package ru.usb.springbootcbrfmpr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.usb.dailyinfo.wsdl.GetCursOnDateResponse;
import ru.usb.dailyinfo.wsdl.GetLatestDateTimeResponse;
import ru.usb.springbootcbrfmpr.Config.Config;
import ru.usb.springbootcbrfmpr.Service.SoapClient;


@SpringBootApplication
@EnableScheduling
public class SpringBootCbrfMprApplication {
	@Autowired
	static String getCursOnDateXml;
	static String getLatestDateTimeXml;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootCbrfMprApplication.class, args);
		SoapClient soapClient = new SoapClient();


		String date = soapClient.sendSoap(Config.getLatestDateTimeXml, GetLatestDateTimeResponse.class);
		getCursOnDateXml = Config.generateCursOnDateXmlRequest(date);
		soapClient.sendSoap(getCursOnDateXml, GetCursOnDateResponse.class);

	}

}
