package br.antoniodiego.navegador;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;

public class MainActivity extends AppCompatActivity {
    //  private Jsoup js;
    private Document d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(br.antoniodiego.navegador.R.layout.atividade_principal);

        final EditText campoEd = findViewById(br.antoniodiego.navegador.R.id.campoEndere);
        Button btAb = findViewById(br.antoniodiego.navegador.R.id.btAbrir);
        btAb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = campoEd.getText().toString();
                try {
                    AbreP ab = new AbreP(url);
                    new Thread(ab).start();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        });

        //  addContentView(new AreaRenderHTML(this),LayoutInflater);
    }

    private void escreveArq(InputStream en) throws IOException {
        File des = new File("sdCard/pagy.html");
        FileOutputStream sai = new FileOutputStream(des);
        BufferedWriter escr = new BufferedWriter(new OutputStreamWriter(sai));
        BufferedReader r = new BufferedReader(new InputStreamReader(en));
        String linha;
        while ((linha = r.readLine()) != null) {
            //   cont.append(linha);
            escr.write(linha);
        }
    }

    private class AbreP implements Runnable {

        private URL url;


        public AbreP(String url) throws MalformedURLException {
            this.url = new URL(url);
        }

        public void setUrl(URL url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection connH = (HttpURLConnection) url.openConnection();
                connH.getHeaderFields();
                String respMsg = connH.getResponseMessage();
                int respC = connH.getResponseCode();
                System.out.println("Cod R: " + respC);
                System.out.println("Msg R: " + respMsg);
                //TODO: Toast
                if (respC == 301) {
                    System.out.println("Movido permanentemente");
                    String locat = connH.getHeaderField("Location");
                    if (locat != null) {
                        System.out.println("Location encontrado");
                        System.out.println("Novo local: " + locat);
                        AbreP red = new AbreP(locat);
                        new Thread(red).start();
                        return;
                    }
                }

                //TODO: Automatizar charset e uri
                d = Jsoup.parse(connH.getInputStream(), "UTF-8", url.getPath());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class AreaRenderHTML extends View {
        private Paint p;

        public AreaRenderHTML(Context context, AttributeSet conj) {
            super(context,conj);
            p = new Paint();
        }

        public AreaRenderHTML(Context context, AttributeSet conj,int style) {
            super(context,conj);
            p = new Paint();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            p.setColor(0x00ff00);
            if (d == null) return;
            Elements tagsH = d.head().getElementsByTag("title");
            canvas.drawText(tagsH.first().text(), 15, 15, p);
        }
    }
}
