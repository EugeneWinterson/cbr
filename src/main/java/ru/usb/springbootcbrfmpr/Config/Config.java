package ru.usb.springbootcbrfmpr.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;


@Configuration
public class Config {

    public static String getLatestDateTimeXml = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="http://web.cbr.ru/">
               <soapenv:Header/>
               <soapenv:Body>
                  <web:GetLatestDateTime/>
               </soapenv:Body>
            </soapenv:Envelope>""";
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("ru.usb.dailyinfo.wsdl");
        return  marshaller;
    }

    public static String generateCursOnDateXmlRequest(String date) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://web.cbr.ru/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:GetCursOnDate>\n" +
                "         <web:On_date>" + date + "</web:On_date>\n" +
                "      </web:GetCursOnDate>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }

}
