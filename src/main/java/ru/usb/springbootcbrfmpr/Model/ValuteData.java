package ru.usb.springbootcbrfmpr.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ValuteData {
    String vName;
    String vNom;
    String vCurs;
    String vCode;
    String vChCode;

    public ValuteData(String vName, String vNom, String vCurs, String vCode, String vChCode) {
        this.vName = vName;
        this.vNom = vNom;
        this.vCurs = vCurs;
        this.vCode = vCode;
        this.vChCode = vChCode;
    }
}
