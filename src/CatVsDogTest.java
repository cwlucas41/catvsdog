import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class CatVsDogTest {
	
	@Test
	public void a() {
		test("sample");
	}
	
	@Test
	public void b() {
		test("goldwasser");
	}
	
	void test(String prefix) {
		
		String inFileName = prefix + ".in";
		String ansFileName = prefix + ".ans";
		String myAnsFileName = prefix + ".myans";
		String errFileName = prefix + ".err";
		String myErrFileName = prefix + ".myerr";

		InputStream stdin = System.in;
		PrintStream stdout = System.out;
		PrintStream stderr = System.err;
		
		InputStream inIStream = null;
		try {
			inIStream = new FileInputStream(inFileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintStream myAnsOStream = null;
		try {
			myAnsOStream = new PrintStream(myAnsFileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PrintStream myErrOStream = null;
		try {
			myErrOStream = new PrintStream(myErrFileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.setIn(inIStream);
		System.setOut(myAnsOStream);
		System.setErr(myErrOStream);
		
		CatVsDog.main(null);
		
		System.setIn(stdin);
		System.setOut(stdout);
		System.setErr(stderr);
		
		try {
			inIStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myAnsOStream.close();
		
		String myAns = null;
		try {
			myAns = new String(Files.readAllBytes(Paths.get(myAnsFileName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String ans = null;
		try {
			ans = new String(Files.readAllBytes(Paths.get(ansFileName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String myErr = null;
		try {
			myErr = new String(Files.readAllBytes(Paths.get(myErrFileName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String err = null;
		try {
			err = new String(Files.readAllBytes(Paths.get(errFileName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("------" + prefix + " TEST------");
		System.out.println(ans);
		System.out.println(err);
		System.out.println(myAns);
		System.out.println(myErr);
		
		assertTrue(myAns.equals(ans));
	}
}
