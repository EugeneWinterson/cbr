package ru.usb.springbootcbrfmpr.Service;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import ru.usb.dailyinfo.wsdl.GetCursOnDateResponse;
import ru.usb.dailyinfo.wsdl.GetLatestDateTimeResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class SoapClient extends WebServiceGatewaySupport {

    String URL = "http://www.cbr.ru/DailyInfoWebServ/DailyInfo.asmx";
    private static final Logger log = LoggerFactory.getLogger(SoapClient.class);

    public SoapClient() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("ru.usb.dailyinfo.wsdl");
        setMarshaller(marshaller);
        setUnmarshaller(marshaller);
    }

    public String sendSoap(String xml, Class soapMethod) {
        try {
            String result = "";

            SOAPMessage soapMessage = prepareSoap(xml);
            JAXBContext jaxbContext = JAXBContext.newInstance(soapMethod);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            if (soapMethod == GetLatestDateTimeResponse.class) {
                GetLatestDateTimeResponse response = (GetLatestDateTimeResponse) unmarshaller
                        .unmarshal(soapMessage.getSOAPBody().getFirstChild());
                        log.info("response: " + response.getGetLatestDateTimeResult());
                result = response.getGetLatestDateTimeResult().toString();
            } else {
                GetCursOnDateResponse response = (GetCursOnDateResponse) unmarshaller
                        .unmarshal(soapMessage.getSOAPBody().getFirstChild());
                Object rows = response.getGetCursOnDateResult().getAny();

                StringWriter stringWriter = new StringWriter();
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.marshal(response, stringWriter);
                String xmlString = stringWriter.toString();
                result = xmlString;
               // log.info("response: " + data.getGetCursOnDateResult().getAny().toString());
            }
            return result;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    SOAPMessage prepareSoap(String xml) throws IOException, SOAPException {
        URL obj = new URL(URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type","text/xml;charset=utf-8");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(xml);
        wr.flush();
        wr.close();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                con.getInputStream()));
        return MessageFactory.newInstance().createMessage(null,
                new ByteArrayInputStream(bufferedReader.readLine().getBytes()));
    }
}
