package main.java;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortByField {

	static class Cmp<E> implements Comparator<E> {
		Method getMethod = null;
		Field fieldToGet = null;
		MethodHandle cmpMethodHandle;
		// Method cmpMethod ;

		Cmp(MethodHandle cmpMethodHandle, /* Method cmpMethod, */Method getMethod) {
			this.getMethod = getMethod;
			this.cmpMethodHandle = cmpMethodHandle;
			// this.cmpMethod = cmpMethod;
		}

		Cmp(MethodHandle cmpMethodHandle, Field fieldToGet) {
			this.cmpMethodHandle = cmpMethodHandle;
			this.fieldToGet = fieldToGet;
		}

		@Override
		public int compare(E o1, E o2) {
			if (o2 == null) {
				return -1;
			} else if (o1 == null) {
				return 1;
			}
			try {
				if (getMethod != null) {
					Object obj1=getMethod.invoke(o1);
					Object obj2=getMethod.invoke(o2);
					if (obj1 == null && obj2 == null) {
					    return 0;
					}
					if (obj1 == null) {
					    return 1;
					}
					if (obj2 == null) {
					    return -1;
					}
					if(obj1.equals(obj2)) {
						return 0;
					}
					
					return (int) cmpMethodHandle.invokeExact((Comparable<?>) obj1, obj2);
					
					// return (int)cmpMethod.invoke(getMethod.invoke(o1),
				}
				if (fieldToGet != null) {
					return (int) cmpMethodHandle.invokeExact((Comparable<?>) fieldToGet.get(o1), fieldToGet.get(o2));
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				System.out.println("If sortByField() isn't modifie, it won't print errorStackTrace. Default return 0");
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return 0;
		}
	}

	/**
	 * sort a list containing JavaBean according specific key( field ).  
	 * Mostly, sortByField take ~1.5 times as much as Traditional implementation when list.size() > 100K
	 * 
	 * @param list:
	 *            list to be sorted
	 * @param fieldName:
	 *            sort list according this field
	 * @param order:
	 *            asc(default) or desc
	 * @author Tony
	 * @email 360517703@163.com
	 * @Time 2015-08-14 11:12
	 */
	public static <E> void sortByField(List<E> list, String fieldName, String order) {
		if (list == null || list.size() < 2) { // no need to sort
			return;
		}
		if (fieldName == null || fieldName.trim().equals(""))
			return; // won't sort if fieldName is null or ""
		// get actual class of generic E
		Class<?> eClazz = null; // use reflect to get the actual class
		boolean isAllNull = true; // default all elements are null
		for (E e : list) {
			if (e != null) {
				isAllNull = false;
				eClazz = e.getClass();
				break;
			}
		}
		if (isAllNull)
			return; // no need to sort, because all elements are null
		// check fieldName in Class E
		Field keyField = null; // the <fieldName> Field as sort key
		try {
			keyField = eClazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e1) {
			e1.printStackTrace();
			System.out.println("The List<E> doesn't contain fieldName. That is "
					+ String.format("%s has no Field %s.", eClazz, fieldName));
		} catch (SecurityException e1) {
			e1.printStackTrace();
			System.out.println("deny access to  class or field.");
		}
		// check field is either Comparable
		Class<?> fieldClazz = keyField.getType();
		boolean isComparable = Comparable.class.isAssignableFrom(fieldClazz);
		if (isComparable == false)
			return; // if the class of fieldName is not comparable, don't sort

		// try to use getter method to get field first. Because a little faster
		// than Field.get(Object)
		StringBuilder getterName; // adapt to JavaBean getter method
		if (fieldClazz.getSimpleName().equals("Boolean")) {
			getterName = new StringBuilder("is");
		} else {
			getterName = new StringBuilder("get");
		}
		char[] cs = fieldName.toCharArray();
		if (cs[0] >= 'a' && cs[0] <= 'z')
			cs[0] -= 32; // change the first char to lowerCase
		getterName.append(cs);
		Method getterMethod = null;
		try {
			getterMethod = eClazz.getDeclaredMethod(getterName.toString());
		} catch (NoSuchMethodException | SecurityException e1) {
			// System.out.println("Field " + fieldName + " has no " + getterName
			// + "() . ");
			// e1.printStackTrace();
		}
		/*
		 * // get compare method for specified field. //Abandoned. Because
		 * MethodHandle.invokeExact() is a little faster than Method.invoke()
		 * Method cmpMethod = null; try { cmpMethod =
		 * fieldClazz.getDeclaredMethod("compareTo", fieldClazz); } catch
		 * (NoSuchMethodException | SecurityException e1) { System.out.println(
		 * "deny access to class or method(comparaTo).\nImpossible to show errorStrackTrace Because of Comparable check"
		 * ); e1.printStackTrace(); cmpMethod.setAccessible(true); }
		 */
		Cmp<E> cmp;
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType type = MethodType.methodType(int.class, Object.class);
		MethodHandle mh = null;
		try {
			mh = lookup.findVirtual(Comparable.class, "compareTo", type);
		} catch (NoSuchMethodException | IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (getterMethod != null) {
			// cmpMethod.setAccessible(true);
			getterMethod.setAccessible(true);
			cmp = new Cmp<E>(mh, getterMethod);
		} else { // if cannot find getter method, use reflect to get specified
					// field
			keyField.setAccessible(true);
			cmp = new Cmp<E>(mh, keyField);
		}

		if (order.equalsIgnoreCase("desc")) {
			Collections.sort(list, Collections.reverseOrder(cmp));
			return;
		}
		Collections.sort(list, cmp);
	}

	/**
	 * sort a list containing JavaBean according specific key( field ) order by
	 * ascend.  
	 * 
	 *  Mostly, sortByField take ~1.5 times as much as Traditional implementation when list.size() > 100K
	 * @param list
	 *            list to be sort
	 * @param fieldName
	 *            sort list according this field
	 * @author Tony
	 * @email 360517703@163.com
	 * @Time 2015-08-14 11:12
	 */
	public static <E> void sortByField(List<E> list, String fieldName) {
		sortByField(list, fieldName, "asc");
	}

}
