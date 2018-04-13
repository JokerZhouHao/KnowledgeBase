package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneTest02 {
	public static void main(String[] args) throws Exception{
		String usage = "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
		if(args.length>0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}
		
		String index = "index";
		String field = "contents";
		String queries = null;
		int repeat = 0;
		boolean raw = false;
		String queryString = null;
		int hitsPerPage = 10;
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		
		BufferedReader in = null;
		if(queries!=null) {
			in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
		} else {
			in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
		}
		QueryParser parser = new QueryParser(field, analyzer);
		while(true) {
			if(queries==null && queryString==null) {
				System.out.println("Enter query : ");
			}
			
			String line = queryString != null ? queryString : in.readLine();
			
			if(line==null || line.length() == -1) {
				break;
			}
			
			line = line.trim();
			if(line.length()==0) {
				break;
			}
			
			Query query = parser.parse(line);
			System.out.println("Search for : " + query.toString());
			
			if(repeat>0) {
				Date start = new Date();
				for(int i=0; i<repeat; i++) {
					searcher.search(query, 100);
				}
				Date end = new Date();
				System.out.println("Time : " + (end.getTime() - start.getTime()) + "ms");
			}
			
			doPagingSearch(in, searcher, query, hitsPerPage, raw, queries==null && queryString==null);
			
			if(queryString!=null) {
				break;
			}
		}
		reader.close();
	}
	
	public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, int hitsPerPage, boolean raw, boolean interactive) throws Exception{
		TopDocs results = searcher.search(query, 5*hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;
		
		int numTotalHits = Math.toIntExact(results.totalHits);
		System.out.println(numTotalHits + " total matching documents");
		
		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);
		while(true) {
			if(end>hits.length) {
				System.out.println("Only results 1 - " + hits.length + " of " + numTotalHits + " total matching documents collected.");
				System.out.println("Collect more (y/n) ?");
				String line = in.readLine();
				if(line.length()==0 || line.charAt(0) == 'n') {
					break;
				}
				hits = searcher.search(query, numTotalHits).scoreDocs;
			}
			
			end = Math.min(hits.length, start + hitsPerPage);
			
			for(int i=start; i<end; i++) {
				if(raw) {
					System.out.println("doc = " + hits[i].doc + " score=" + hits[i].score);
					continue;
				}
				
				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("path");
				if(path!=null) {
					System.out.println((i+1) + ". " + path);
					String title = doc.get("title");
					if(title!=null) {
						System.out.println(" Title: " + doc.get("title"));
					}
				} else {
					System.out.println((i+1) + ". " + "No path for this document");
				}
			} 
			
			if(!interactive || end==0) {
				break;
			}
		}
	}
}























