package org.search;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.*;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.similarities.*;

public class search {
    private static String INDEX_DIRECTORY = "../search_eng/index";
    private static int MAX_RESULTS = 50;
    public static void main(String[] args) throws Exception {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        isearcher.setSimilarity(new ClassicSimilarity());
        CharArraySet stopWords = CharArraySet.copy(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        Analyzer analyzer = new Sword(stopWords);
        Map<String, Float> boost = Boost();
        MultiFieldQueryParser qp = new MultiFieldQueryParser(new String[] {"title", "published", "author", "content"}, analyzer, boost);
        ArrayList<String> query = Getquery();
        ArrayList<String> vars = new ArrayList<String>();

        //search
        for(int i =0; i<query.size(); i++){
            String q = query.get(i);
            //Remove Spaces at the beginning and end
            q = q.trim();
            if (q.length()>0){
                Query que = null;
                //Text translation to prevent special characters
                String st = QueryParser.escape(q);
                //use st to search
                que = qp.parse(st);
                ScoreDoc[] hits = isearcher.search(que, MAX_RESULTS).scoreDocs;
                for (int j = 0; j < hits.length; j++) {
                    Document hitDoc = isearcher.doc(hits[j].doc);
                    int rank = j+1;
                    //double noms = normScore("Other", hits[j].score);
                    double noms = hits[j].score;
                    if (noms >0){
                        vars.add(i+1 + " 0 " + hitDoc.get("index") + " "+ rank + " "+ noms  +" Classic \n");
                    }
                }
            }
        }

        writeToFile(vars);
        ireader.close();
        directory.close();

    }

    //read query form dataset
    public static ArrayList<String> Getquery() throws IOException {
        String Path = "../search_eng/corpus/cran.qry";
        ArrayList<String> q = new ArrayList<String>();
        FileReader fileReader = new FileReader(Path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = "";
        int index = 0;
        while (line != null) {
            index = index + 1;
            String qry = "";
            line = bufferedReader.readLine();
            while (line.contains(".I") || line.contains(".W")) {
                line = bufferedReader.readLine();
            }
            while (line != null && !line.contains(".I")) {
                qry  = qry +  " " + line;
                line = bufferedReader.readLine();
            }
            q.add(qry);
        }
        return q;
    }

    //Create weighted values for each field
    private static Map<String, Float> Boost(){
        Map<String, Float> boost = new HashMap<>();
        boost.put("title", (float) 0.5);
        boost.put("published",(float) 0.1);
        boost.put("author", (float) 0.1);
        boost.put("content", (float) 0.6);
        return boost;
    }

    private static void writeToFile(ArrayList<String> results) throws IOException {
        BufferedWriter writer = null;
        File file = new File("result.txt");
        writer = new BufferedWriter(new FileWriter(file));
        for (String res : results){
            writer.write(res);
            }
        writer.close();
    }

    private static double normScore(String eval, double score){
        return score;

    }
}
