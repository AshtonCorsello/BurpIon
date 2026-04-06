import burp.api.montoya.http.message.HttpRequestResponse; 
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor; 
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.Selection;

import com.amazon.ion.IonReader; 
import com.amazon.ion.IonSystem; 
import com.amazon.ion.IonWriter; 
import com.amazon.ion.system.IonSystemBuilder;
import com.amazon.ion.system.IonTextWriterBuilder;


import javax.swing.*; 
import java.awt.*; 

public class IonEditorTab implements ExtensionProvidedHttpResponseEditor {
    private final JTextArea textArea; 
    private HttpResponse currentResponse; 

    public IonEditorTab() {
        textArea = new JTextArea(); 
        textArea.setEditable(false); 
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    @Override
    public String caption() {
        return "Ion"; 
    }

    @Override
    public Component uiComponent() {
        return new JScrollPane(textArea); 
    }

    @Override 
    public boolean isEnabledFor(HttpRequestResponse requestResponse){
        if (requestResponse.response() == null) {
            return false; 
        }

        byte[] body = requestResponse.response().body().getBytes(); 
        return body.length >= 4
            && (body[0] & 0xFF) == 0xE0
            && (body[1] & 0xFF) == 0x01
            && (body[2] & 0xFF) == 0x00
            && (body[3] & 0xFF) == 0xEA;
    }

    @Override 
    public void setRequestResponse(HttpRequestResponse requestResponse) {
        currentResponse = requestResponse.response(); 
        byte[] body = currentResponse.body().getBytes(); 

        try {
            IonSystem ionSystem = IonSystemBuilder.standard().build();
            IonReader reader = ionSystem.newReader(body); 
            StringBuilder sb = new StringBuilder(); 

            IonWriter writer = IonTextWriterBuilder.pretty().build(sb); 

            while (reader.next() != null) {
                boolean isSymbolTable = false;
                for (String annotation : reader.getTypeAnnotations()) {
                    if ("$ion_symbol_table".equals(annotation)) {
                        isSymbolTable = true;
                        break;
                    }
                }
                if (!isSymbolTable) {
                    writer.writeValue(reader);
                }
            }
            writer.close();

            textArea.setText(sb.toString()); 
        } catch (Exception e) {
            textArea.setText("Failed to parse Ion Data: " + e.getMessage()); 
        }
    }

    @Override 
    public HttpResponse getResponse() {
        return currentResponse; 
    }

    @Override 
    public Selection selectedData() {
        return null; 
    }

    @Override
    public boolean isModified() {
        return false; 
    }



}