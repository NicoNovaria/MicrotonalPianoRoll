/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.nico.microtonalpianoroll;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.TriangleOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.softsynth.shared.time.TimeStamp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JOptionPane;
import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Rest;
import jm.music.data.Score;
import jm.util.Play;
import jm.util.View;
import jm.util.Write;


/**
 *
 * @author Nick
 */
public class MicrotonalPianoRoll extends JFrame implements ActionListener, JMC, KeyEventDispatcher{

    //COMPONENTI GRAFICHE
    public JLabel labelSelectedNote;
    public JLabel lbInsertDiv;
    public static JTextArea txtaNT = new JTextArea();
    
    public static JButton btnReset = new JButton();
    public static JButton btnPlay = new JButton(); 
    public static JButton btnRec = new JButton();
    public static JButton btnSave = new JButton();
    public static JButton btnLoad = new JButton();
    public JButton btnUP = new JButton(); 
    public JButton btnDOWN = new JButton();
    
    JTextField txtFmin = new JTextField(5);
    JTextField txtFmax = new JTextField(5);
    
    private JPanel panelNorth = new JPanel();
    private JPanel panelEast = new JPanel();
    private JPanel panelCenter = new JPanel();
    private JPanel panelSouth = new JPanel();
    public ScrollPane scrPane = new ScrollPane();
    
    //######
    
    //suddivisioni dei tasti, frequenze, ecc...
    public static double div;
    public static double Fmin;
    public static double Fmax;
    public static double freq;
    public static ArrayList<Double> note = new ArrayList<Double>();
    public static ArrayList<Double> rhythm = new ArrayList<Double>();
    public static boolean recording = false;
    
    public static double range;
    //########
    
    //JSYN
    public static Synthesizer synth;
    public static Synthesizer SY;
    public static UnitOscillator osc;
    public static UnitOscillator OSC;
    public static LineOut lineOut;
    public static LineOut LO;
    public static SegmentedEnvelope envelope;
    public static VariableRateMonoReader envPlayer;
    public static int instrument = 0;
    
    public static String wave = "Sine Wave";
    public static double attack = 0.02;
    public static double decay = 0.10;
    public static double release = 0.50;
    
    static boolean keyPressed = false;
    static boolean bPause = false;
    static boolean bNote = true;
    
    static String data;
    //######
    
    
    
    
    //JMUSIC
    public static Part part = new Part();
    public static Phrase phrase = new Phrase();
    public static Score score = new Score();
    //#####
    
    
    public MicrotonalPianoRoll() {
        
        super("NotesButtons");
        setSize(700, 200);
        setMinimumSize(new Dimension (700, 400));
        setPreferredSize(new Dimension(1200, 800));
        
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        setLayout(new BorderLayout());

        panelNorth.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        add(panelNorth, BorderLayout.NORTH);
        
        lbInsertDiv = new JLabel("Divisioni:");
        lbInsertDiv.setFont(new Font("Arial", Font.BOLD, 18));
        lbInsertDiv.setToolTipText("Massimo 28 divisioni consentite");
        
        JTextField txtDiv = new JTextField(5);
        
        
        JLabel lbFmin = new JLabel("Fmin:");
        JLabel lbFmax = new JLabel("Fmax:");
        lbFmin.setFont(new Font("Arial", Font.BOLD, 18));
        lbFmax.setFont(new Font("Arial", Font.BOLD, 18));

        
        txtFmin = new JTextField(5);
        txtFmax = new JTextField(5);
        
        
        JButton btnOk = new JButton("OK");
        
        
        //BTN CHE SCATENA IL DRAW DEGLI ALTRI COMPONENTI
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    
                    if (txtFmin.getText().isEmpty() || txtFmax.getText().isEmpty() || txtDiv.getText().isEmpty()){
                        JOptionPane.showMessageDialog(null, "Tutti i campi di divisione, Fmin e Fmax devono essere valorizzati.", "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    if (Integer.parseInt(txtDiv.getText()) > 28 || Integer.parseInt(txtDiv.getText()) <= 0){
                        JOptionPane.showMessageDialog(null, "Il range consentito di divisioni è [1-28].", "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    
                    Fmin = Double.parseDouble(txtFmin.getText());
                    Fmax = Double.parseDouble(txtFmax.getText());
                    
                    range = Fmax-Fmin+1;
                    
                    if (Fmin < 20.0 || Fmax > 20000.0){
                        JOptionPane.showMessageDialog(null, "Fmin e Fmax devono essere valorizzati nel range '20-20000'.", "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    
                    //check che Fmax sia maggiore di Fmin
                    if(Fmin >= Fmax){
                        JOptionPane.showMessageDialog(null, "Fmin deve avere un valore MINORE di Fmax per un corretto funzionamento.", "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    

                    div = Double.parseDouble(txtDiv.getText());
                    draw();

                } catch(NumberFormatException x){  
                    JOptionPane.showMessageDialog(null, "Divisione, Fmin e Fmax devono essere NUMERI interi o decimali, non sono ammessi caratteri speciali oltre al '.'", "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
                    return;  
                }
            } 
        });
        
        
        //BTN per istruzioni per l'applicativo
        JButton btnInfo = new JButton("?");
        btnInfo.setToolTipText("Clicca per maggiori informazioni.");
        btnInfo.setBackground(Color.CYAN);
        
        btnInfo.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "L'applicativo permette di generare una tastiera microtonale inserendo parametri a scelta dall'utente e poter suonare con essa.\n\n"
                        + "PARAMETRI INIZIALI:\n"
                        + "1) DIVISIONI prevede l'inserimento di un numero nel range [1-28] e determina il numero di suddivisioni frequenziali della tastiera;\n"
                        + "2) FMIN e FMAX stabiliranno il range frequenziale (20-20k Hz) e determineranno, assieme al valore di divisione, le frequenze associate a ciascun pulsante della tastiera.\n "
                        + "Alla pressione del tasto OK, se i parametri inseriti rispetteranno le condizioni precedentemente citate, l'utente si troverà a disposizione nella "
                        + "sezione centrale dell'interfaccia grafica i pulsanti di una pseudo\n tastiera di pianoforte che, tramite una pressione del mouse, genererà un suono con "
                        + "frequenza microtonale associata.\n "
                        + "Oltre al click con il mouse, l'utente potrà generare il suono attraverso la pressione dei tasti della tastiera del pc, essi sono:\n\n"
                        + "                q w e r t y u i o p è a s d f g h j k l ò z x c v b n m ,\n"
                        + "    il pulsante < verrà utilizzato per generare una PAUSA (maggiori dettagli in seguito)\n\n"
                        + "Nella sezione a destra vengono presentati i comandi utili a modificare il suono real-time; l'utente potrà scegliere di generare un'onda sinusoidale, quadra, triangolare o a dente di sega.\n"
                        + "L'onda generata in real-time sarà poi gestita tramite un'inviluppo; con gli slider si potranno modificare i tempi di ATTACK, DECAY e RELEASE (con valori tra 0.1 e 1). Il SUSTAIN non è gestito tramite slider dato che\n"
                        + "la nota continuerà a suonare finchè l'utente terrà premuto un pulsante.\n"
                        + "Tramite + e - verrà consentito lo shift del range frequenziale.\n\n"
                        + "Nella parte inferiore della schermata troveremo invece le componenti atte alla performance che ci permetteranno di RIPRODURRE, SALVARE, CARICARE e RESETTARE la performance musicale precedentemente REGISTRATA tramite\n"
                        + "gli appositi pulsanti. La registrazione viene avviata dopo aver premuto il pulsante REC, e viene gestita tramite due principali modalità: \n\n"
                        + "1) L'utente decide di registrare la performance premento col MOUSE i pulsanti nell'interfaccia grafica.\n"
                        + "2) L'utente decide di registrare la performance premendo i pulsanti della TASTIERA del PC.\n\n"
                        + "In entrambi i casi, per associare il valore ritmico alla NOTA precedentemente suonata (o alla PAUSA), bisogenrà premere i pulsanti da tastiera del pc [1, 2, 3, 4, 8] DOPO aver suonato una nota per assegnarle rispettivamente\n"
                        + "il valore di DURATA [1/4, 2/4, 3/4, 1/8]. La corretta combinazione di operazioni da effettuare per registrare una performance sarà quindi: CLICK NOTA(o pausa) + NUMERO. Una non corretta procedura potrebbe portare\n"
                        + "ad una registrazione non fedele della performance musicale che non sarà caricabile tramite il pulsante LOAD in un secondo momento.\n"
                        + "N.B. il suono generato real-time NON CORRISPONDE alla performance registrata, dato che esso viene gestito da un inviluppo e quindi sarà l'utente a determinare la durata della nota nel momento in cui rilascerà\n"
                        + "il pulsante premuto.\n\n"
                        + "Tramite PLAY si potrà riascoltare l'esecuzione, con RESET si potrà cancellare la performance e ripartire da zero, con SAVE la si potrà salvare in un formato testuale .txt mentre con LOAD potremo andare a ricaricare\n"
                        + "la performance salvata (a patto che il formato sia rispettato) per poi poterla riascoltare premendo .\n"
                        + "La sequenza di note (o pause) registrate con relative durate viene notificata all'utente con una text-box posta in basso alla schermata e aggiornata man mano che la registrazione prosegue.", "MANUALE D'USO:", JOptionPane.INFORMATION_MESSAGE);                
            }
        });
        
        
        
        panelNorth.add(lbInsertDiv);
        panelNorth.add(txtDiv);
        panelNorth.add(lbFmin);
        panelNorth.add(txtFmin);
        panelNorth.add(lbFmax);
        panelNorth.add(txtFmax);
        panelNorth.add(btnOk);
        panelNorth.add(btnInfo);
        
        initSynth();
    }
    
    
    
    public static void main(String[] args) {
        MicrotonalPianoRoll frame = new MicrotonalPianoRoll();
        frame.setVisible(true);
        
        //setta il KeyboardFocusManager per gestire i click sui pulsanti da tastiera di pc
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher( new MicrotonalPianoRoll() );
        
    }
    

    
    //METODI IMPORTATI DALLE INTERFACCE
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (e.getSource() == btnReset){
            note.clear();
            rhythm.clear();
            txtaNT.setText("");
            
            bPause = false;
            bNote = true;
            
            colorBtns();
        }
        
        if(e.getSource() == btnPlay){
            try{
                if((note.isEmpty() || rhythm.isEmpty()) || note.size() != rhythm.size()){
                    JOptionPane.showMessageDialog(null, "La performance inserita non è valida, per una corretta registrazione bisogna specificare la coppia NOTA seguita dalla sua DURATA,\nmaggiori informazioni disponibili premendo il pulsante '?'.", "ATTENZIONE!", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                initS_Playback();
                               
                TimeStamp timeStamp = SY.createTimeStamp();
                LO.start();

                Double[] arrNote = note.toArray(new Double[0]);
                Double[] arrRhyt = rhythm.toArray(new Double[0]);
                
                int i = 0;
                //possiamo ciclare solo su note.size dato che note e rhythm hanno ugual dijmensione 
                while (i < note.size()) {
                    if (i >= note.size())
                        break;

                    double freq = arrNote[i];
                    double duration = arrRhyt[i];
                    
                    
                    if(freq == 0.0)
                        OSC.noteOn(freq, 0.0, timeStamp);
                    else
                        OSC.noteOn(freq, 0.9, timeStamp);
                    
                    OSC.noteOff(timeStamp.makeRelative(duration));
                    
                    timeStamp = timeStamp.makeRelative(duration);
                    i++;
                }
                
                
            }
            catch(Exception ex){
                JOptionPane.showMessageDialog(null, "Errore: "+ ex.toString(), "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        if(e.getSource() == btnRec){
            recording = !recording;
            colorBtns();
        }
        
        if(e.getSource() == btnUP)
            shiftOct("UP");

        
        if(e.getSource() == btnDOWN)  
            shiftOct("DOWN");
        
        if(e.getSource() == btnSave){
            //N.B. genera il file testuale, per sentire il risultato audio bisogna caricarlo nell'app tramite il pulsante Load
            
            try {
                FileWriter myWriter = new FileWriter(System.getProperty("user.dir")+ "\\performance.txt");
                myWriter.write(txtaNT.getText());
                myWriter.close();
                JOptionPane.showMessageDialog(null, "Il file è stato correttamente salvato nella directory " + System.getProperty("user.dir"), "SUCCESSO!", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException i) {
                JOptionPane.showMessageDialog(null, "Errore: "+ i.toString(), "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
        }
        
        if(e.getSource() == btnLoad){
            note.clear();
            rhythm.clear();
            
            try {
                File myObj = new File(System.getProperty("user.dir")+ "\\performance.txt");
                Scanner myReader = new Scanner(myObj);
                
                while (myReader.hasNextLine()) {
                  data = myReader.nextLine();
                  //System.out.println(data);
                }
                myReader.close();
                

                //ripulitura della struttura testuale
                String d = data.replace("[", "");
                String f = d.replace("]", "");           
                String[] x = f.split(",");


                for(int i = 0; i < x.length; i++){
                    String[] n = x[i].split(" ");

                    for(int j = 0; j < n.length; j++){

                        if(j == 0)
                            if(n[j].charAt(0) == 'P')
                                note.add(0.0);
                            else
                                note.add(Double.parseDouble(n[j]));
                        else{
                            switch(n[j]){
                                case "1/4":
                                    rhythm.add(C);
                                    break;
                                case "2/4":
                                    rhythm.add(M);
                                    break;
                                case "3/4":
                                    rhythm.add(DM);
                                    break;
                                case "4/4":
                                    rhythm.add(SB);
                                    break;
                                case "1/8":
                                    rhythm.add(Q);
                                    break;
                            }
                        }
                    }
                }
                
                
            } catch (FileNotFoundException f) {
              JOptionPane.showMessageDialog(null, f.getMessage() + "\nAssicurarsi che il salvataggio della performance musicale sia avvenuto almeno una volta e che il file specificato si trovi nella directory corretta.", "FILE NON TROVATO!", JOptionPane.ERROR_MESSAGE);             
              return;
            } catch (Exception g) {
              JOptionPane.showMessageDialog(null, "Il file che stai cercando di caricare non rispetta il formato supportato da questa applicazione: [NOTA, DURATA].\nRESETTA la performace e riprova.", "FILE NON VALIDO!", JOptionPane.ERROR_MESSAGE);             
              return;
            }
            
            txtaNT.setText(data);
            txtaNT.setBackground(Color.yellow);
            colorBtns();
            
        }     
        
    }
    
    
    //LISTENER SULLA KEYBOARD
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        
        
        if(e.getID() == KeyEvent.KEY_PRESSED) {

            switch(e.getKeyChar()){
                /*la condizione if(!keyPressed) è necessaria perchè, se l'utente tenesse un pulsante della tastiera del pc
                premuto, continuerebbe a chiamare il metodo playnote(n) il che genererebbe una grande quantità di note;
                in questo modo viene bloccato il metodo finchè il tasto premuto non verrà rilasciato*/
                
                case 'q':
                    if (!keyPressed){ 
                        playNote(0);
                        keyPressed = true;
                    }
                    break;                   
                case 'w':
                    if (!keyPressed){ 
                        playNote(1);
                        keyPressed = true;
                    }
                    break;
                case 'e':
                    if (!keyPressed){ 
                        playNote(2);
                        keyPressed = true;
                    }
                    break;
                case 'r':
                    if (!keyPressed){ 
                        playNote(3);
                        keyPressed = true;
                    }
                    break;
                case 't':
                    if (!keyPressed){ 
                        playNote(4);
                        keyPressed = true;
                    }
                    break;
                case 'y':
                    if (!keyPressed){ 
                        playNote(5);
                        keyPressed = true;
                    }
                    break;
                case 'u':
                    if (!keyPressed){ 
                        playNote(6);
                        keyPressed = true;
                    }
                    break;
                case 'i':
                    if (!keyPressed){ 
                        playNote(7);
                        keyPressed = true;
                    }
                    break;
                case 'o':
                    if (!keyPressed){ 
                        playNote(8);
                        keyPressed = true;
                    }
                    break;
                case 'p':
                    if (!keyPressed){ 
                        playNote(9);
                        keyPressed = true;
                    }
                    break;
                case 'è':
                    if (!keyPressed){ 
                        playNote(10);
                        keyPressed = true;
                    }
                    break;
                case 'a':
                    if (!keyPressed){ 
                        playNote(11);
                        keyPressed = true;
                    }
                    break;
                case 's':
                    if (!keyPressed){ 
                        playNote(12);
                        keyPressed = true;
                    }
                    break;
                case 'd':
                    if (!keyPressed){ 
                        playNote(13);
                        keyPressed = true;
                    }
                    break;
                case 'f':
                    if (!keyPressed){ 
                        playNote(14);
                        keyPressed = true;
                    }
                    break;
                case 'g':
                    if (!keyPressed){ 
                        playNote(15);
                        keyPressed = true;
                    }
                    break;
                case 'h':
                    if (!keyPressed){ 
                        playNote(16);
                        keyPressed = true;
                    }
                    break;
                case 'j':
                    if (!keyPressed){ 
                        playNote(17);
                        keyPressed = true;
                    }
                    break;
                case 'k':
                    if (!keyPressed){ 
                        playNote(18);
                        keyPressed = true;
                    }
                    break;
                case 'l':
                    if (!keyPressed){ 
                        playNote(19);
                        keyPressed = true;
                    }
                    break;
                case 'ò':
                    if (!keyPressed){ 
                        playNote(20);
                        keyPressed = true;
                    }
                    break;
                case 'z':
                    if (!keyPressed){ 
                        playNote(21);
                        keyPressed = true;
                    }
                    break;
                case 'x':
                    if (!keyPressed){ 
                        playNote(22);
                        keyPressed = true;
                    }
                    break;
                case 'c':
                    if (!keyPressed){ 
                        playNote(23);
                        keyPressed = true;
                    }
                    break;
                case 'v':
                    if (!keyPressed){ 
                        playNote(24);
                        keyPressed = true;
                    }
                    break;
                case 'b':
                    if (!keyPressed){ 
                        playNote(25);
                        keyPressed = true;
                    }
                    break;
                case 'n':
                    if (!keyPressed){ 
                        playNote(26);
                        keyPressed = true;
                    }
                    break;
                case 'm':
                    if (!keyPressed){ 
                        playNote(27);
                        keyPressed = true;
                    }
                    break;
                case ',':
                    if (!keyPressed){ 
                        playNote(28);
                        keyPressed = true;
                    }
                    break;
                    
                 case '<':
                    if(recording && bNote){
                        note.add(0.0);
                        txtaNT.setText(txtaNT.getText() + "[P");
                        
                        bNote = !bNote;
                        bPause = !bPause;
                    }
                    break;
                    
                case '1':
                    if(recording && !keyPressed && bPause){                      
                        rhythm.add(C);
                        txtaNT.setText(txtaNT.getText() + " 1/4],");
                        keyPressed = true;
                        
                        bNote = !bNote;
                        bPause = !bPause;
                        
                    }
                    break;
                case '2':
                    if(recording && !keyPressed  && bPause){   
                        rhythm.add(M);
                        txtaNT.setText(txtaNT.getText() + " 2/4],");
                        keyPressed = true;
                        
                        bNote = !bNote;
                        bPause = !bPause;
                    }
                    break;
                case '3':
                    if(recording && !keyPressed  && bPause){ 
                        rhythm.add(DM);
                        txtaNT.setText(txtaNT.getText() + " 3/4],");
                        keyPressed = true;
                        
                        bNote = !bNote;
                        bPause = !bPause;
                    }
                    break;
                case '4':
                    if(recording && !keyPressed  && bPause){ 
                        rhythm.add(SB);
                        txtaNT.setText(txtaNT.getText() + " 4/4],");
                        keyPressed = true;
                        
                        bNote = !bNote;
                        bPause = !bPause;
                    }
                    break;
                case '8':
                    if(recording && !keyPressed  && bPause){ 
                        rhythm.add(Q);
                        txtaNT.setText(txtaNT.getText() + " 1/8],");
                        keyPressed = true;
                        
                        bNote = !bNote;
                        bPause = !bPause;
                    }
                    break;

            }
            colorBtns();
            
            
        }
        else if(e.getID() == KeyEvent.KEY_RELEASED){
            envPlayer.dataQueue.queue(envelope, 2, 1);
            keyPressed = false;
        }
          // allow the event to be redispatched
          return false;
    }
    
    //######
    
    
    
    
    //DISEGNA IL RESTO DELLA GUI
    private void draw(){
        //Scrollable Panel
        //scrPane.setSize(500, 300);
        scrPane.setWheelScrollingEnabled(true);      
        
        //pannello inserito nello scrollable panel contenente i btns per generazione note
        panelCenter.removeAll();
        panelEast.removeAll();
        panelSouth.removeAll();
        //setSize(800, 800);
        
        panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.X_AXIS));        
        panelEast.setLayout(new BoxLayout(panelEast, BoxLayout.Y_AXIS));    
        panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));
        
        //GENERAZIONE BTNs, <= div consente di generare anche il btn con la frequenza massima inserita dall'utente che sarebbe altrimenti scartata
        for (int i = 0; i <= div; i++) {
            final int x = i;
            
            JButton btnNote = new JButton();
            btnNote.setMargin(new Insets(10,10,150,10));
            panelCenter.add(btnNote);
            
            //frequenza calcolata per mostrare il tooltip a ciascun tasto
            //valutare se invece inserire il valore visibile direttamente sul testo del btn
            final double freqToolTip = Fmin * Math.pow(Fmax / Fmin, x/div);
            btnNote.setToolTipText(String.valueOf(freqToolTip));
            
            
            
            //variante con mouse premuto e rilasciato
            btnNote.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent evt) {
                    playNote(x);
                    /*if(recording){
                    rhythm.add(C);
                    txtaNT.setText(txtaNT.getText() + " 1/4,");
                    }*/
                }
                @Override
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    try{
                    envPlayer.dataQueue.queue(envelope, 2, 1);
                    }
                    catch(Exception ex){
                        JOptionPane.showMessageDialog(null, "Errore: "+ ex.toString(), "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            });    
            
        }
        
        
        //COMANDI PANEL EAST
        JLabel lbWaveF = new JLabel("Wave form:");
        lbWaveF.setFont(new Font("Arial", Font.BOLD, 18));
        
        JList jlWaves = new JList<>();
        jlWaves.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jlWaves.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Sine Wave", "Square Wave", "Triangle Wave", "Sawtooth Wave" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jlWaves.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jlWaves.setToolTipText("Definisce il tipo di forma d'onda");
        jlWaves.setSelectedIndices(new int[] {0});
        jlWaves.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                wave = (String) jlWaves.getSelectedValue();
                synth.stop();
                initSynth();
            }
        });
        
        panelEast.add(lbWaveF);
        panelEast.add(jlWaves);
        
        
        JLabel lbA = new JLabel("Attack:");
        JLabel lbD = new JLabel("Decay:");
        JLabel lbR = new JLabel("Release:");
        
        JSlider jsA = new JSlider();
        JSlider jsD = new JSlider();
        JSlider jsR = new JSlider();
        
        jsA.setMajorTickSpacing(50);
        jsA.setMinimum(1);
        jsA.setMinorTickSpacing(1);
        jsA.setOrientation(javax.swing.JSlider.HORIZONTAL);
        jsA.setPaintTicks(true);
        jsA.setSnapToTicks(true);
        jsA.setToolTipText("Tempo di ATTACK del segnale con un valore tra 0.1 e 1");
        jsA.setValue(20);
        jsA.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jsA.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                attack = jsA.getValue()/100.0d;
                synth.stop();
                initSynth();
            }
        });
        jsA.setBounds(1030, 500, 50, 140);

        
        
        jsD.setMajorTickSpacing(50);
        jsD.setMinimum(1);
        jsD.setMinorTickSpacing(1);
        jsD.setOrientation(javax.swing.JSlider.HORIZONTAL);
        jsD.setPaintTicks(true);
        jsD.setToolTipText("Tempo di DECAY del segnale con un valore tra 0.1 e 1");
        jsD.setValue(10);
        jsD.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jsD.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                decay = jsD.getValue()/100.0d;
                synth.stop();
                initSynth();
            }
        });
        jsD.setBounds(1110, 500, 50, 140);
        
        
        jsR.setMajorTickSpacing(50);
        jsR.setMinimum(1);
        jsR.setMinorTickSpacing(1);
        jsR.setOrientation(javax.swing.JSlider.HORIZONTAL);
        jsR.setPaintTicks(true);
        jsR.setToolTipText("Tempo di RELEASE del segnale con un valore tra 0.1 e 1");
        jsR.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jsR.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                release = jsR.getValue()/100.0d;
                synth.stop();
                initSynth();
            }
        });
        jsR.setBounds(1210, 500, 50, 140);
        
        panelEast.add(lbA);
        panelEast.add(jsA);
        panelEast.add(lbD);
        panelEast.add(jsD);
        panelEast.add(lbR);
        panelEast.add(jsR);
        
        JLabel lbUP = new JLabel("Shift Up:");
        lbUP.setFont(new Font("Arial", Font.BOLD, 18));
        btnUP = new JButton("+");
        btnUP.setToolTipText("Shifta le frequenze Fmin e Fmax in ALTO.");
        btnUP.addActionListener(this);
        
        JLabel lbDOWN = new JLabel("Shift Down:");
        lbDOWN.setFont(new Font("Arial", Font.BOLD, 18));
        btnDOWN = new JButton("-");
        btnDOWN.setToolTipText("Shifta le frequenze Fmin e Fmax in BASSO.");
        btnDOWN.addActionListener(this);
        
        panelEast.add(lbUP);
        panelEast.add(btnUP);
        panelEast.add(lbDOWN);
        panelEast.add(btnDOWN);
        //####
        
        
        
        //COMANDI PANEL SOUTH
        btnRec = new JButton("Rec");
        btnRec.addActionListener(this);
        
        btnPlay = new JButton("Play");
        btnPlay.setToolTipText("SUONA la performance musicale registrata.");
        btnPlay.addActionListener(this);
        
        btnReset = new JButton("Reset");      
        btnReset.setToolTipText("RESETTA la performance musicale registrata.");
        btnReset.addActionListener(this);
        
        

        btnSave = new JButton("Save");
        btnSave.setToolTipText("SALVA la performance musicale registrata nel formato .mid nella directory " + System.getProperty("user.dir"));
        btnSave.addActionListener(this);
        
        
        btnLoad = new JButton("Load");
        btnLoad.setToolTipText("CARICA la performance musicale salvata nel formato dalla directory " + System.getProperty("user.dir"));
        btnLoad.addActionListener(this);
        
        
        colorBtns();
        
        //txtaNT = new JTextArea();
        txtaNT.setLineWrap(true);
        txtaNT.setWrapStyleWord(true);
        txtaNT.setEditable(false);
        
        panelSouth.add(btnRec);
        panelSouth.add(btnPlay);
        panelSouth.add(btnReset);
        panelSouth.add(btnSave);
        panelSouth.add(btnLoad);
        panelSouth.add(txtaNT);
        //#####
        
        scrPane.add(panelCenter);

        add(scrPane, BorderLayout.CENTER);
        add(panelEast, BorderLayout.EAST);
        add(panelSouth, BorderLayout.SOUTH);

        pack();
    }
    
    
    
    //GENERAZIONE NOTE
    private void playNote(double n){
        if(n > div){
            return;
        }

        freq = Fmin * Math.pow(Fmax / Fmin, n/div);
        
        
        if(recording && bNote){
            
            note.add(freq);  
            
            double round = Math.round(freq*Math.pow(10,2))/Math.pow(10,2);
            txtaNT.setText(txtaNT.getText() + "[" + String.valueOf(round));
            
            bPause = !bPause;
            bNote = !bNote;
            
            colorBtns();        //serve richiamarlo anche in questo punto dato che la nota suonata può essere generata anche tramite click col mouse
        }
        
        
        try{
            osc.frequency.set(freq);
            envPlayer.dataQueue.clear();
            envPlayer.dataQueue.queue(envelope, 0, 1);
            envPlayer.dataQueue.queueLoop(envelope, 1, 1);
        }
        catch(Exception ex){
            JOptionPane.showMessageDialog(null, "Errore: "+ ex.toString(), "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    
    
    
    /*SYNTH*/
    private void initSynth() {
        synth = JSyn.createSynthesizer();
        synth.start();
        
        synth.remove(osc); 
        
        //cambio della forma d'onda per il segnale
        switch(wave){
            case "Sine Wave":
                //instrument = 0;
                synth.add(osc = new SineOscillator());
                break;
            
            case "Square Wave":
                //instrument = 80;
                synth.add(osc = new SquareOscillator());
                break;
                
            case "Triangle Wave":
                //instrument = 2;
                synth.add(osc = new TriangleOscillator());
                break;
                
            case "Sawtooth Wave":
                //instrument = 81;
                synth.add(osc = new SawtoothOscillatorBL());
                break;
        }
        
        
        synth.add(lineOut = new LineOut());
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        // Create an envelope and fill it with recognizable data.
        double[] data
                = {
                    attack, 0.9, // attack
                    decay, 0.7, // decay
                    release, 0.0 // release
                };
        envelope = new SegmentedEnvelope(data);

        // Hang at end of decay segment to provide a "sustain" segment.
        envelope.setSustainBegin(1);
        envelope.setSustainEnd(1);

        synth.add(envPlayer = new VariableRateMonoReader());
        /* Connect envelope to oscillator amplitude. */
        envPlayer.output.connect(osc.amplitude);
        lineOut.start();
    }

   
    
    
    private void initS_Playback() {
        SY = JSyn.createSynthesizer();
        
        SY.remove(OSC); 
        switch(wave){
            case "Sine Wave":
                SY.add(OSC = new SineOscillator());
                break;
            
            case "Square Wave":
                SY.add(OSC = new SquareOscillator());
                break;
                
            case "Triangle Wave":
                SY.add(OSC = new TriangleOscillator());
                break;
                
            case "Sawtooth Wave":
                SY.add(OSC = new SawtoothOscillatorBL());
                break;
        }
        
        SY.add(LO = new LineOut());

        OSC.getOutput().connect(0, LO.input, 0);
        OSC.getOutput().connect(0, LO.input, 1);

        SY.start();
    }
    
    
    
    /*Metodo per gestire la corretta colorazione e tooltips per i btns di PERFORMANCE*/
    private void colorBtns(){
        if(recording){
            btnRec.setToolTipText("Premi per USCIRE dalla modalità REGISTRAZIONE.");
            btnRec.setForeground(Color.red);
            
            txtaNT.setBackground(Color.white);
        }
        else{
            btnRec.setToolTipText("Premi per ENTRARE in modalità REGISTRAZIONE.");
            btnRec.setForeground(Color.black);
        }
        
        if((!note.isEmpty() && !rhythm.isEmpty()) && note.size() == rhythm.size()){
            btnPlay.setForeground(Color.green);
            btnPlay.setEnabled(true);
            
            //sempre attivo
            btnReset.setForeground(Color.blue);
            
            btnSave.setForeground(Color.pink);
            btnSave.setEnabled(true);

        }
        else{
            btnPlay.setForeground(Color.black);
            btnPlay.setEnabled(false);
            
            //sempre attivo
            btnReset.setForeground(Color.black);
            
            btnSave.setForeground(Color.black);
            btnSave.setEnabled(false);
            
            txtaNT.setBackground(Color.white);
        }
    
    }
    
    
    
    /*Metodo per gestire lo SHIFT di frequenza*/
    private void shiftOct(String s){
        //lo shift è gestito prendendo in considerazione il range di frequenza
        //Fmax-Fmin che consente uno shifting lineare, nel caso di SHIFTUP il valore di Fmin sarà quello della precedente Fmax, 
        //Fmax a sua volta verrà incrementato del range frequenzale
        //viceversa per SHIFTDOWN si avrà il comportamento opposto
        range = Fmax-Fmin+1;
        
        switch(s){
            case "UP":
                if (Fmax + range > 20000.0){
                    JOptionPane.showMessageDialog(null, "Fmin e Fmax devono essere valorizzati nel range '20-20000'.", "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Fmin = Fmax+1;
                Fmax = Fmax + range;
                break;
                
            case "DOWN":
                if (Fmin - range < 20.0){
                    JOptionPane.showMessageDialog(null, "Fmin e Fmax devono essere valorizzati nel range '20-20000'.", "ATTENZIONE!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Fmax = Fmin-1;
                Fmin = Fmin - range;
                break;

        }
        
        txtFmin.setText(String.valueOf(Fmin));
        txtFmax.setText(String.valueOf(Fmax));
        draw();
    }
    
    
}
