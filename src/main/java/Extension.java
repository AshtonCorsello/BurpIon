import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class Extension implements BurpExtension {

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("Ion Viewer");

        montoyaApi.userInterface().registerHttpResponseEditorProvider(new IonTabFactory()); 
    }
}