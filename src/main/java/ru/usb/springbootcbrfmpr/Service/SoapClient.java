package ru.usb.cbrtompr.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.w3c.dom.Node;
import ru.usb.cbrtompr.Config.Config;
import ru.usb.cbrtompr.Model.ValuteData;
import ru.usb.dailyinfo.wsdl.GetCursOnDateResponse;
import ru.usb.dailyinfo.wsdl.GetLatestDateTimeResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

@Service
public class SoapClient extends WebServiceGatewaySupport {

    String URL = "http://www.cbr.ru/DailyInfoWebServ/DailyInfo.asmx";
    String INSERT_QUERY = "INSERT INTO MPREXT.CURRATES(TITLE_RU,NOMINAL,VAL,NUM_CODE,TITLE_EN, ACTUALDATE) VALUES(?,?,?,?,?,?)";
    private static final Logger log = LoggerFactory.getLogger(SoapClient.class);
    @Autowired
    private JdbcTemplate oraMPRTemplate;
    private String curDate;
    private Node valuteCursOnDate, valuteData;

    public SoapClient() {

    }

    @Scheduled(cron = "${cron}")
    public void StartProcess() {
        SoapClient soapClient = new SoapClient();
        curDate = soapClient.sendSoap(Config.getLatestDateTimeXml, GetLatestDateTimeResponse.class);
        if(checkDate(curDate)) {
            log.info("Start Process - start");
            String getCursOnDateXml = Config.generateCursOnDateXmlRequest(curDate);
            soapClient.sendSoap(getCursOnDateXml, GetCursOnDateResponse.class);
        }
    }

    public String sendSoap(String xml, Class<?> soapMethod) {
        try {
            log.info("process -> sendSoap");
            String result;
            SOAPMessage soapMessage = prepareSoap(xml);
            if (soapMethod == GetLatestDateTimeResponse.class) {
                String response = soapMessage.getSOAPBody().getFirstChild().getTextContent();
                log.info("response: {}",  response);
                result = response;
            } else {
                valuteCursOnDate = soapMessage.getSOAPBody().extractContentAsDocument()
                        .getFirstChild().getFirstChild().getFirstChild().getNextSibling().getFirstChild();
                sendDataToMPR();
                result = "OK";
            }
            return result;
        } catch (Exception e) {
            log.error(e.getMessage());
            return "sendSoap Error: " + e.getMessage();
        }
    }

    //Подготовка SOAP-запроса
    SOAPMessage prepareSoap(String xml) {
        try {
            URL obj = new URL(URL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
            con.setDoOutput(true);
            log.info("prepareSoap -> XML: " + xml);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(xml);
            wr.flush();
            wr.close();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            return MessageFactory.newInstance().createMessage(null,
                    new ByteArrayInputStream(bufferedReader.readLine().getBytes()));
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    ArrayList<ValuteData> getValuteDataFromSOAP() {
        ArrayList<ValuteData> arrayList = new ArrayList<>();
        int count = Integer.parseInt(valuteCursOnDate.getLastChild().getAttributes().item(1).getNodeValue());
        for (int i = 0; i <= count; i++) {
            String vName = valuteData.getFirstChild().getFirstChild().getNodeValue().trim();
            String vNom = valuteData.getFirstChild().getNextSibling().getFirstChild().getNodeValue().trim();
            String vCurs = valuteData.getFirstChild().getNextSibling().getNextSibling().getFirstChild().getNodeValue().trim();
            String vCode = valuteData.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNodeValue().trim();
            String vChCode = valuteData.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNodeValue().trim();
            valuteData = valuteData.getNextSibling();
            arrayList.add(new ValuteData(vName, vNom, vCurs, vCode, vChCode));
        }
        log.info("getValuteDataFromSOAP: " + arrayList.size());
        return arrayList;
    }

    void sendDataToMPR() throws SQLException {
        try {
            valuteData = valuteCursOnDate.getFirstChild();
            ArrayList<ValuteData> arrayList = getValuteDataFromSOAP();
            Connection connection = Objects.requireNonNull(oraMPRTemplate.getDataSource()).getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_QUERY);
            for (ValuteData valuteData : arrayList) {
                log.info(valuteData.toString());
                preparedStatement.setString(1, valuteData.getVName());
                preparedStatement.setString(2, valuteData.getVNom());
                preparedStatement.setString(3, valuteData.getVCurs());
                preparedStatement.setString(4, valuteData.getVCode());
                preparedStatement.setString(5, valuteData.getVChCode());
                preparedStatement.setString(6, curDate);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            log.info("sendDataToMPR -> " + "Insert success");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    Boolean checkDate(String date) {
        String dateWithoutTime = date.substring(0, date.indexOf("T"));
        LocalDate localDate = LocalDate.parse(dateWithoutTime);
        LocalDate curDate = LocalDate.now();
        return curDate.getDayOfMonth() < localDate.getDayOfMonth();
    }
}
