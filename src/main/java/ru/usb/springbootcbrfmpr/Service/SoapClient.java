package ru.usb.springbootcbrfmpr.Service;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.w3c.dom.Node;
import ru.usb.dailyinfos.wsdl.GetLatestDateTimeResponse;
import ru.usb.springbootcbrfmpr.Model.ValuteData;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

@Service
public class SoapClient extends WebServiceGatewaySupport {

    String url = "http://www.cbr.ru/DailyInfoWebServ/DailyInfo.asmx";
    private static final Logger log = LoggerFactory.getLogger(SoapClient.class);

    public SoapClient() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("ru.usb.dailyinfo.wsdl");
        setMarshaller(marshaller);
        setUnmarshaller(marshaller);
    }

    public String sendSoap(String xml, Class<?> soapMethod) {
        try {
            String result = "";

            SOAPMessage soapMessage = prepareSoap(xml);
            if (soapMethod == GetLatestDateTimeResponse.class) {
                var response = soapMessage.getSOAPBody().getFirstChild().getTextContent();
                log.info("response: {}",  response);
                result = response;
            } else {
                Node valuteCursOnDate = soapMessage.getSOAPBody().extractContentAsDocument()
                        .getFirstChild().getFirstChild().getFirstChild().getNextSibling().getFirstChild();
                Node valuteData = valuteCursOnDate.getFirstChild();
                var count = Integer.parseInt(valuteCursOnDate.getLastChild().getAttributes().item(1).getNodeValue());
                ArrayList<ValuteData> arrayList = new ArrayList<>();
                for(int i =0; i <= count; i++) {
                    var vName = valuteData.getFirstChild().getFirstChild().getNodeValue().trim();
                    var vNom = valuteData.getFirstChild().getNextSibling().getFirstChild().getNodeValue().trim();
                    var vCurs = valuteData.getFirstChild().getNextSibling().getNextSibling().getFirstChild().getNodeValue().trim();
                    var vCode = valuteData.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNodeValue().trim();
                    var vChCode = valuteData.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNodeValue().trim();
                    valuteData = valuteData.getNextSibling();
                    arrayList.add(new ValuteData(vName, vNom, vCurs , vCode, vChCode));
                }

                for(int i = 0; i < arrayList.size(); i++) {
                    //prepare statement
                    //add batch
                }
                //execute batch
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
