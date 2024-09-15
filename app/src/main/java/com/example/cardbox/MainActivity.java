package com.example.cardbox;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    sqliteDB db;
    int letters = 0;
    String sqlQuery = "1 = 1";
    int mode = 0;
    String label = "(No Action)";
    HashMap<String, String> colourList;
    HashMap<String, String> dictionary;
    HashMap<String, Integer> anagramsList;

    TextView t1;
    TextView t2;
    Button b1;
    Button b2;
    Button b3;
    Button b4;
    Button b5;
    Button b6;
    Button b7;
    Button b8;
    Button b9;
    Button b10;
    Button b11;

    GridView g1;
    Spinner s1;

    ArrayList<String> anagrams;
    int words;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1 = findViewById(R.id.textview1);
        t2 = findViewById(R.id.textview2);
        b1 = findViewById(R.id.button1);
        b2 = findViewById(R.id.button2);
        b3 = findViewById(R.id.button3);
        b4 = findViewById(R.id.button4);
        b5 = findViewById(R.id.button5);
        b6 = findViewById(R.id.button6);
        b7 = findViewById(R.id.button7);
        b8 = findViewById(R.id.button8);
        b9 = findViewById(R.id.button9);
        b10 = findViewById(R.id.button10);
        b11 = findViewById(R.id.button11);

        db = new sqliteDB(MainActivity.this);

        g1 = findViewById(R.id.gridview1);
        s1 = findViewById(R.id.spinner1);

        ArrayList<String> labelsList = db.getAllLabels();
        colourList = db.getColours();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppData", 0);
        boolean prepared = pref.getBoolean("prepared", false);

        if(prepared) {
            getWordLength();
        } else {
            db.alertBox("Database initialization", "Please give 1 hour to prepare database of dictionary words. Only when opening this for the first time.", MainActivity.this);
            db.prepareScores();
            prepareDictionary();
        }

        ArrayAdapter<String> comboBoxAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, labelsList);
        comboBoxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(comboBoxAdapter);

        s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                label = labelsList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSqlQuery();
            }
        });

        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View yourCustomView = inflater.inflate(R.layout.query, null);

                TextView t4 = yourCustomView.findViewById(R.id.textview3);
                t4.setText(db.getSchema());

                EditText e5 = yourCustomView.findViewById(R.id.edittext2);

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Enter your SQL query")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String sqlQuery = (e5.getText()).toString();
                                db.myQuery(sqlQuery, MainActivity.this);

                                if(mode == 1)
                                {
                                    anagrams = db.getAllAnagrams(letters);
                                    words = anagrams.size();
                                    counter = db.getCounter(Integer.toString(letters));

                                    int peak = (words - 1) / 100;
                                    if (counter > peak && words > 0) {
                                        counter = peak;
                                        db.updateScores(Integer.toString(letters), counter);
                                    }
                                }
                                else if(mode == 2)
                                {
                                    anagrams = db.getSqlQuery(sqlQuery, MainActivity.this);
                                    words = anagrams.size();
                                    String query = "SELECT word, definition, back, front FROM words WHERE " + sqlQuery;
                                    counter = db.getCounter(query);

                                    int peak = (words - 1) / 100;
                                    if (counter > peak && words > 0) {
                                        counter = peak;
                                        db.updateScores(query, counter);
                                    }
                                }

                                ArrayList<String> labelsList = db.getAllLabels();
                                colourList = db.getColours();

                                ArrayAdapter<String> comboBoxAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, labelsList);
                                comboBoxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                s1.setAdapter(comboBoxAdapter);

                                s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                        label = labelsList.get(i);
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapterView) {
                                    }
                                });

                                if(mode == 1)
                                {
                                    nextWord();
                                }
                                else if(mode == 2)
                                {
                                    String customQuery = "SELECT word, definition, back, front FROM words WHERE " + sqlQuery;
                                    executeSqlQuery(customQuery);
                                }
                            }
                        }).create();
                dialog.show();
            }
        });

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String labelColours = db.getLabelColours();
                db.messageBox("Label colours", labelColours, MainActivity.this);
            }
        });

        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.exportLabels(MainActivity.this);
            }
        });

        b9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.importLabels(MainActivity.this);
            }
        });

        b10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.exportDB(MainActivity.this);
            }
        });

        b11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.importDB(MainActivity.this);
            }
        });
    }

    public void prepareDictionary()
    {
        dictionary = new HashMap<>();
        anagramsList = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("CSW2021.txt"), "UTF-8"));
            while(true)
            {
                String s = reader.readLine();
                if(s == null)
                {
                    break;
                }
                else
                {
                    String t[] = s.split("=");
                    dictionary.put(t[0], t[1]);

                    char jumbled[] = t[0].toCharArray();
                    Arrays.sort(jumbled);
                    String solution = new String(jumbled);

                    if(anagramsList.containsKey(solution))
                    {
                        anagramsList.put(solution, anagramsList.get(solution) + 1);
                    }
                    else
                    {
                        anagramsList.put(solution, 1);
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        prepareDatabase();
    }

    public void prepareDatabase()
    {
        Iterator<Map.Entry<String, String>> itr = dictionary.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String word = entry.getKey();
            char c[] = word.toCharArray();
            Arrays.sort(c);
            String anagram = new String(c);
            int solutions = anagramsList.get(anagram);
            String definition = entry.getValue();
            StringBuilder back = new StringBuilder();
            StringBuilder front = new StringBuilder();
            for(char letter = 'A'; letter <= 'Z'; letter++)
            {
                if(dictionary.containsKey(word + letter))
                {
                    back.append(letter);
                }
                if(dictionary.containsKey(letter + word))
                {
                    front.append(letter);
                }
            }
            boolean q = db.insertWord(word, word.length(), anagram, definition, probability(word), new String(back), new String(front), solutions);
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppData", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("prepared", true);
        editor.commit();

        setPageNumbers();
        insertColours();
        getWordLength();
    }

    public void setPageNumbers()
    {
        for(int lengths = 2; lengths <= 15; lengths++)
        {
            ArrayList<String> anagramList = db.getAllAnagrams(lengths);
            db.updatePageNumbers(anagramList);
        }
    }

    public void insertColours()
    {
        db.insertColour("Known", "#00C000");
        db.insertColour("Unknown", "#FF0000");
        db.insertColour("Compound", "#FF00C0");
        db.insertColour("Prefix", "#C000FF");
        db.insertColour("Suffix", "#8080FF");
        db.insertColour("Plural", "#808080");
        db.insertColour("Guessable", "#FF8000");
        db.insertColour("Past", "#00C0FF");
        db.insertColour("Learnt", "#B97A57");
        db.insertColour("New", "#FFFF00");
        db.insertColour("", "#FFFFFF");
    }

    public void updateGridView()
    {
        List<String> wordsList = anagrams.subList(counter * 100, Math.min((counter + 1) * 100, anagrams.size()));
        HashMap<String, ArrayList<String>> jumbles = db.getAllWords(wordsList);

        customadapter cusadapter = new customadapter(MainActivity.this, R.layout.cell, wordsList, jumbles);
        g1.setAdapter(cusadapter);

        g1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedWord = wordsList.get(i);
                ArrayList<String> chosenWord = jumbles.get(selectedWord);

                String meaning = chosenWord.get(0);
                String category = chosenWord.get(2);

                TextView t7 = view.findViewById(R.id.textview2);
                TextView t8 = view.findViewById(R.id.textview3);

                if(label.equals("(No Action)"))
                {
                    if(colourList.containsKey(category)) {
                        if((colourList.get(category)).equals("#FFFFFF")) {
                            t2.setText(Html.fromHtml(meaning + " <b>" + (category.length() == 0 ? "(No Label)" : category) + "</b>"));
                        } else {
                            t2.setText(Html.fromHtml("<font color=\"" + colourList.get(category) + "\">" + meaning + " <b>" + (category.length() == 0 ? "(No Label)" : category) + "</b></font>"));
                        }
                    } else if(colourList.containsKey("")) {
                        if((colourList.get("")).equals("#FFFFFF")) {
                            t2.setText(Html.fromHtml(meaning + " <b>" + (category.length() == 0 ? "(No Label)" : category) + "</b>"));
                        } else {
                            t2.setText(Html.fromHtml("<font color=\"" + colourList.get("") + "\">" + meaning + " <b>" + (category.length() == 0 ? "(No Label)" : category) + "</b></font>"));
                        }
                    } else {
                        t2.setText(Html.fromHtml(meaning + " <b>" + (category.length() == 0 ? "(No Label)" : category) + "</b>"));
                    }
                }
                else
                {
                    db.updateLabel(selectedWord, label);
                    (jumbles.get(selectedWord)).set(2, label);
                    String newColour = colourList.get(label);

                    t7.setBackgroundColor(Color.parseColor(newColour));
                    t8.setBackgroundColor(Color.parseColor(newColour));

                    if(newColour.equals("#FFFFFF")) {
                        t2.setText(Html.fromHtml(meaning + " <b>" + (label.length() == 0 ? "(No Label)" : label) + "</b>"));
                    } else {
                        t2.setText(Html.fromHtml("<font color=\"" + newColour + "\">" + meaning + " <b>" + (label.length() == 0 ? "(No Label)" : label) + "</b></font>"));
                    }
                }
            }
        });

        g1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedWord = wordsList.get(i);

                char character[] = selectedWord.toCharArray();
                Arrays.sort(character);
                String order = new String(character);

                String allAnagrams = db.getAllAnswers(order);

                db.messageBox("All anagrams", allAnagrams, MainActivity.this);
                return true;
            }
        });
    }

    public void getWordLength()
    {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View yourCustomView = inflater.inflate(R.layout.input, null);

        EditText e1 = yourCustomView.findViewById(R.id.edittext1);
        e1.setHint("Enter a value between 2 and 15");

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Word length")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String alphabet = (e1.getText()).toString();
                        letters = alphabet.length() == 0 ? 0 : Integer.parseInt(alphabet);
                        if(letters < 2 || letters > 15)
                        {
                            Toast.makeText(MainActivity.this, "Enter a value between 2 and 15", Toast.LENGTH_LONG).show();
                            getWordLength();
                        }
                        else
                        {
                            mode = 1;

                            start();
                        }
                    }
                }).create();
        dialog.show();
    }

    public void getSqlQuery()
    {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View yourCustomView = inflater.inflate(R.layout.query, null);

        EditText e2 = yourCustomView.findViewById(R.id.edittext2);

        TextView t3 = yourCustomView.findViewById(R.id.textview3);
        t3.setText(db.getSchema());

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("SELECT front, word, back, definition FROM words WHERE")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        sqlQuery = (e2.getText()).toString();
                        execute();
                    }
                }).create();
        dialog.show();
    }

    public void start()
    {
        anagrams = db.getAllAnagrams(letters);
        words = anagrams.size();
        counter = db.getCounter(Integer.toString(letters));

        nextWord();
    }

    public void execute()
    {
        ArrayList<String> resultSet = db.getSqlQuery(sqlQuery, MainActivity.this);

        if(resultSet != null) {
            mode = 2;

            anagrams = resultSet;
            words = anagrams.size();
            String query = "SELECT word, definition, back, front FROM words WHERE " + sqlQuery;
            int exist = db.getExist(query);

            if (exist == 0) {
                counter = 0;
                db.insertScores(query, counter);
            } else {
                counter = db.getCounter(query);
            }

            executeSqlQuery(query);
        }
    }

    public void nextWord()
    {
        b1.setEnabled(true);
        b2.setEnabled(true);
        b3.setEnabled(true);
        b5.setEnabled(true);

        t1.setText("Page " + (counter + 1) + " out of " + (((words - 1) / 100) + 1));
        t2.setText("");

        updateGridView();

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter--;
                if(counter < 0)
                {
                    counter = (words - 1) / 100;
                }
                db.updateScores(Integer.toString(letters), counter);
                nextWord();
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter++;
                if(counter == ((words - 1) / 100) + 1)
                {
                    counter = 0;
                }
                db.updateScores(Integer.toString(letters), counter);
                nextWord();
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View yourCustomView = inflater.inflate(R.layout.input, null);

                EditText e3 = yourCustomView.findViewById(R.id.edittext1);
                int maximum = ((words - 1) / 100) + 1;
                e3.setHint("Enter a value between 1 and " + maximum);

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Go to page")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String pages = (e3.getText()).toString();
                                int page = pages.length() == 0 ? 0 : Integer.parseInt(pages);
                                if(page < 1 || page > maximum)
                                {
                                    Toast.makeText(MainActivity.this, "Enter a value between 1 and " + maximum, Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    counter = page - 1;
                                    db.updateScores(Integer.toString(letters), counter);
                                    nextWord();
                                }
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    public void executeSqlQuery(String query)
    {
        b1.setEnabled(true);
        b2.setEnabled(true);
        b5.setEnabled(true);

        t1.setText("Page " + (counter + 1) + " out of " + (((words - 1) / 100) + 1));
        t2.setText("");

        updateGridView();

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter--;
                if(counter < 0)
                {
                    counter = (words - 1) / 100;
                }
                db.updateScores(query, counter);
                executeSqlQuery(query);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter++;
                if(counter == ((words - 1) / 100) + 1)
                {
                    counter = 0;
                }
                db.updateScores(query, counter);
                executeSqlQuery(query);
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View yourCustomView = inflater.inflate(R.layout.input, null);

                EditText e4 = yourCustomView.findViewById(R.id.edittext1);
                int maximum = ((words - 1) / 100) + 1;
                e4.setHint("Enter a value between 1 and " + maximum);

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Go to page")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String pages = (e4.getText()).toString();
                                int page = pages.length() == 0 ? 0 : Integer.parseInt(pages);
                                if(page < 1 || page > maximum)
                                {
                                    Toast.makeText(MainActivity.this, "Enter a value between 1 and " + maximum, Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    counter = page - 1;
                                    db.updateScores(query, counter);
                                    nextWord();
                                }
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    public class customadapter extends ArrayAdapter<String>
    {
        Context con;
        int _resource;
        List<String> lival1;
        HashMap<String, ArrayList<String>> lival2;

        public customadapter(Context context, int resource, List<String> li1, HashMap<String, ArrayList<String>> li2) {
            super(context, resource, li1);
            // TODO Auto-generated constructor stub
            con = context;
            _resource = resource;
            lival1 = li1;
            lival2 = li2;
        }

        @Override
        public View getView(int position, View v, ViewGroup vg)
        {
            View vi = null;
            LayoutInflater linflate = (LayoutInflater)(MainActivity.this).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = linflate.inflate(_resource, null);

            TextView t5 = vi.findViewById(R.id.textview2);
            TextView t6 = vi.findViewById(R.id.textview3);

            String lival = lival1.get(position);
            ArrayList<String> h = lival2.get(lival);
            String livalue = h.get(1);
            String cardbox = h.get(2);

            t5.setText(lival);
            t6.setText(livalue);

            String li = colourList.containsKey(cardbox) ? colourList.get(cardbox) : (colourList.containsKey("") ? colourList.get("") : "#FFFFFF");

            t5.setBackgroundColor(Color.parseColor(li));
            t6.setBackgroundColor(Color.parseColor(li));

            return vi;
        }
    }

    public double probability(String st)
    {
        int frequency[] = new int[] {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
        int count = 100;
        double chance = 1;
        for(int j = 0; j < st.length(); j++)
        {
            char ch = st.charAt(j);
            int ord = ((int) ch) - 65;
            chance *= frequency[ord];
            chance /= count;
            if(frequency[ord] > 0) {
                frequency[ord]--;
            }
            count--;
        }
        return chance;
    }
}