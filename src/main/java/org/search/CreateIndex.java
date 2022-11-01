package org.search;
import java.io.IOException;
import java.nio.file.Paths;
import java.io.*;
import java.io.BufferedReader;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.search.similarities.*;

public class CreateIndex {
    // Directory where the search index will be saved
    private static String INDEX_DIRECTORY = "../search_eng/index";
    public static void main(String[] args) throws IOException {
        CharArraySet stopwords = CharArraySet.copy(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        Analyzer analyzer = new Sword(stopwords);
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig iw = new IndexWriterConfig(analyzer);
        iw = iw.setSimilarity(new ClassicSimilarity());
        iw.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, iw);
        iwriter = addDocument(iwriter);
        iwriter.close();
        directory.close();

    }
    private static IndexWriter addDocument(IndexWriter iw) {
        String docPath = "../search_eng/corpus/cran.all.1400";
        try {
            FileReader filereader = new FileReader(docPath);
            BufferedReader br = new BufferedReader(filereader);
            String line ="";
            int index=0;
            while(line != null){
                String title = "";
                String author = "";
                String pub = "";
                String words = "";
                index = index +1;
                line = br.readLine();
                System.out.println(index);
                while(!line.contains(".A")){
                    line = br.readLine();
                    if(line.contains(".A")){
                        break;
                    }else if(line.contains(".T")){
                        line = br.readLine();
                    }
                    title = title + line + " ";
                }

                while(!line.contains(".B")){
                    line = br.readLine();
                    if (line.contains(".B")){
                        break;
                    }
                    author = author + line +" ";
                }

                while(!line.contains(".W")){
                    line = br.readLine();
                    if(line.contains(".W")){
                        break;
                    }
                    pub = pub + line + " ";
                }

                while(!line.contains(".I")){
                    line = br.readLine();
                    if (line == null || line.contains(".I")){
                        break;
                    }
                    words = words + line + " ";
                }

                Document doc = createDoc(index, title, author, pub, words);
                iw.addDocument(doc);

            }
            br.close();


        }catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            docPath + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return iw;
    }
    private static Document createDoc(int index, String title, String author, String pub, String words) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("index", index+"", Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("author", author, Field.Store.YES));
        doc.add(new TextField("published", pub, Field.Store.YES));
        doc.add(new TextField("content", words, Field.Store.YES));
        return doc;
    }

}
