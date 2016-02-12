package com.janrain.android.multidex.simpledemonative;

import java.util.ArrayList;

public class LinkData {
    String identifier = null;
    String domainName = null;
    String verifiedEmails = null;

    public LinkData(String identifier, String domainName, String emails) {
        super();
        this.identifier = identifier;
        this.domainName = domainName;
        this.verifiedEmails = emails;
    }

    public LinkData() {
        // TODO Auto-generated constructor stub
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domain) {
        this.domainName = domain;
    }


    public String getVerifiedEmail() {
         return verifiedEmails;
    }

    public void setVerifiedEmail(String verifiedEmails) {
        this.verifiedEmails = verifiedEmails;
    }

}
