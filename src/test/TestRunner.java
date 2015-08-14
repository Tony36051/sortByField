package test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import main.java.SortByField;


public class TestRunner {

	public List<?> compareToClassicImplement(int N) throws NoSuchAlgorithmException{
		//int N = 10000;
		List<User> sortByFieldTestCase = new ArrayList<User>(N);
		List<User> classicTestCase = new ArrayList<User>(N);
		MessageDigest md5 = MessageDigest.getInstance("MD5");	
		for(int i=0; i<N; i++) {
			md5.update(Integer.valueOf(i).toString().getBytes());
			StringBuilder sb = new StringBuilder();
			byte[] buf = md5.digest();
			for (byte b: buf) {
				sb.append(Integer.toHexString(b&0xff));
			}
			User u = new User(sb.toString(), i) ;
			sortByFieldTestCase.add(u);
			classicTestCase.add(u);
		}
		System.out.println("generation finish.");
		
		int rep = 1;
		long a = System.currentTimeMillis();
		for(int i=0; i<rep; i++) {
			SortByField.sortByField(sortByFieldTestCase, "name");
		}
		System.out.println("SortByField done.");
		long b = System.currentTimeMillis();
//		rep = 0;
		for(int i=0; i<rep; i++) {
			Collections.sort(classicTestCase, new Comparator<User>() {
				@Override
				public int compare(User o1, User o2) {
					if (o2 == null) {
						return -1;
					}else if (o1 == null ) {
						return 1;
					}
					return o1.getName().compareTo(o2.getName());
					//return o1.k.compareTo(o2.k);
				}
			});
		}
		System.out.println("Classic implement done.");

		long c = System.currentTimeMillis();
		System.out.println("list.size(): " + N + ". sort "+ rep + " time(s).");
		System.out.println("SortByField: " + (b - a));
		System.out.println("Classic: " + (c - b));
		System.out.printf("SortByField / Classic: %.2f", 1.0*(b-a)/(c-b));
		return sortByFieldTestCase;
		
	}
	
/*	public void performanceMethodVsField() throws Exception{
		int N = 1000000;
		List<User> list = new ArrayList<User>(N);
		MessageDigest md5 = MessageDigest.getInstance("MD5");	
		for(int i=0; i<N; i++) {
			md5.update(Integer.valueOf(i).toString().getBytes());
			StringBuilder sb = new StringBuilder();
			byte[] buf = md5.digest();
			for (byte b: buf) {
				sb.append(Integer.toHexString(b&0xff));
			}
			list.add(new User(sb.toString(), i) );
		}
		System.out.println("generation finish.");
		Method gm = User.class.getDeclaredMethod("getK");
		gm.setAccessible(true);
		Field gf = User.class.getDeclaredField("k");
		gf.setAccessible(true);
		int rep = 100000000;
		long a = System.currentTimeMillis();
		for(int i=0; i<rep; i++) {
			gm.invoke(list.get(i % N));
		}
		System.out.println("method get done.");
		
		long b = System.currentTimeMillis();
		for(int i=0; i<rep; i++) {
			gf.get(list.get(i% N));
			
		}
		
		System.out.println("Field get finish.");
		for(User User : list) {
			System.out.println(User);
			//if (sortedCount++ > N) break;
		}
		long c = System.currentTimeMillis();
		System.out.println("method get : " + (b - a));
		System.out.println("Field get finish: " + (c - b));
	}*/
	public static void main(String[] args) throws Exception  {
		TestRunner tr = new TestRunner();
		tr.compareToClassicImplement(100000);
/*		List<User> list = (List<User>) tr.compareToClassicImplement(10);
		for (User u : list) {0
			System.out.println(u);
		}
		User u = new User("Tony", 10086);
		Field f = u.getClass().getDeclaredField("name");
		f.setAccessible(true);
		System.out.println(f.get(u));*/
	}

}
