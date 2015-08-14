# sortByField
 sort a list of comparable objects by specified field. Usually sorting a list of JavaBean

####Example sort JavaBean:  
  + you get some javabean from database, says mysql.users
  <pre><code> // Javabean
  public class User {
  	private String name;
  	private Integer age;
  	// some setter and getter
  }</code></pre>
  + you store them into a list
  > List\<User\> list = new ArrayList\<User\>();  
  > list.add(/\*...\*/);  
  > // ...    
  
  + you may want to sort them by key "name"; or you may want to sort them by key "age"

######1. Traditional implementation
<pre><code>			Collections.sort(classicTestCase, new Comparator<User>() {
				@Override
				public int compare(User o1, User o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});</code></pre>
In traditional implementation, you write several Comparator. **Ugly and tedious!**
<pre><code>			Collections.sort(classicTestCase, new Comparator<User>() {
				@Override
				public int compare(User o1, User o2) {
					return o1.getAge().compareTo(o2.getAge());
				}
			});</code></pre>
######2. sortByField implementation
 > SortByField.sortByField(list, "name");  
   SortByField.sortByField(list, "age");
  
### Documents:
> public static <E> void sortByField(List<E> list, String fieldName, String order)
+ list: a list
+ fieldName: sort key
+ order: asc or desc.  default is asc  

> public static <E> void sortByField(List<E> list, String fieldName)  
+ list: a list
+ fieldName: sort key

#### Warning:
1. JavaBean must have field named fieldName
2. JavaBean with getter will get better performance than without. Though both situation can work
3. The sort key field must implements Comparable. many Java language built-in class implements Comparable, such as String, Date, Integer ...
4. Mostly, sortByField take ~1.5X time as much as Traditional implementation when list.size() \> 100K
