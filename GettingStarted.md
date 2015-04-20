## Demonstration ##

To see a simple JSimpleDB database in action, download the JSimpleDB distribution ZIP file:
  1. Download and unzip [jsimpledb-1.1.748.zip](https://s3.amazonaws.com/archie-public/jsimpledb/jsimpledb-1.1.748.zip)
  1. Run `java -jar jsimpledb-gui.jar` to start up the GUI on port 8080
  1. Run `java -jar jsimpledb-cli.jar` to start up the command line interface

The distribution ZIP file includes a simple example database containing objects in the solar system. The JSimpleDB command line interface (CLI) and automatic graphical user interface (GUI) tools will auto-detect the presence of the demonstration database in the current directory on startup when no other configuration is specified.

The `demo-classes/` subdirectory contains the Java model classes used to create the example database. The database and GUI are entirely configured from the handful of annotations on these classes. Note that you are free to put these annotations anywhere in the type hierarchy (see for example <a href='http://jsimpledb.googlecode.com/svn/trunk/publish/reports/javadoc/index.html?src-html/org/jsimpledb/demo/Body.html'><code>Body.java</code></a> and <a href='http://jsimpledb.googlecode.com/svn/trunk/publish/reports/javadoc/index.html?src-html/org/jsimpledb/demo/AbstractBody.html'><code>AbstractBody.java</code></a>).

The underlying key/value store for the demo database is an <a href='http://jsimpledb.googlecode.com/svn/trunk/publish/reports/javadoc/index.html?org/jsimpledb/kv/simple/XMLKVDatabase.html'><code>XMLKVDatabase</code></a> instance. `XMLKVDatabase` is a "toy" key/value store implementation that uses a simple flat XML file format for persisting the key/value pairs. That file is `demo-database.xml` and is included in the distribution.

The database was originally loaded using the `load` command in the CLI, which reads files in "object XML" format (in this case, the file was `demo-objs.xml`, also included).

To view the demo database in the auto-generated Vaadin GUI:

```

java -jar jsimpledb-gui.jar```

Then point a web browser to port 8080. If you already have something running on port 8080, add the `--port` flag. Use the `--help` flag to see other command line options.

To view the demo database in the CLI:

```

java -jar jsimpledb-cli.jar```

Use the `help` command to see all commands and functions. In particular, the `expr` command evaluates any Java expression and is used for database queries. For example, to query for all Moon objects, enter `expr all(Moon)`.

The `expr` command supports several extensions to Java syntax, including:
  * Function invocations (the `all()` function used above is an example)
  * Objects may be referred to by object ID literal with a leading at-sign, eg., `@fc02ac0000000001`
  * Session variables have the form `$foo`; you can set them and use them later in other expressions

## Installation ##

See [Downloads](Downloads.md) for how to include JSimpleDB in your build.

## Configuration ##

Creating and configuring a JSimpleDB database requires these steps:
  1. Configure your underlying key/value store
  1. Gather your Java model classes
  1. Create a `JSimpleDB` instance

If you are using Spring you can do this all in XML using JSimpleDB's `<jsimpledb:jsimpledb>` custom XML tag.

Inside the `<jsimpledb:jsimpledb>` is a `<jsimpledb:scan-classes>` tag, which works just like Spring's `<context:component-scan>`, except instead of scanning for classes annotated with `@Component`, etc., it scans for classes annotated with `@JSimpleClass`.

JSimpleDB also provides a Spring `TransactionManager` so you can do all the normal Spring things like `@Transactional`, transaction synchronizations, etc.

Here's an example of a setup that uses an <a href='http://jsimpledb.googlecode.com/svn/trunk/publish/reports/javadoc/index.html?org/jsimpledb/kv/simple/XMLKVDatabase.html'><code>XMLKVDatabase</code></a>:

```xml

<beans xmlns="http://www.springframework.org/schema/beans"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xmlns:jsimpledb="http://jsimpledb.googlecode.com/schema/jsimpledb"
xmlns:tx="http://www.springframework.org/schema/tx"
xmlns:p="http://www.springframework.org/schema/p"
xmlns:c="http://www.springframework.org/schema/c"
xsi:schemaLocation="
http://jsimpledb.googlecode.com/schema/jsimpledb http://jsimpledb.googlecode.com/svn/schemas/jsimpledb-1.0.xsd
http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd">

<!-- Define the underlying key/value store -->
<bean id="kvstore" class="org.jsimpledb.kv.simple.XMLKVDatabase" c:file="/tmp/jsimpledb.xml"/>

<!-- Define the JSimpleDB database on top of that -->
<jsimpledb:jsimpledb id="jsimpledb" kvstore="kvstore" schema-version="1">
<jsimpledb:scan-classes base-package="com.example.myapp.model"/>


Unknown end tag for &lt;/jsimpledb&gt;



<!-- Create a Spring transaction manager -->
<bean id="transactionManager" class="org.jsimpledb.spring.JSimpleDBTransactionManager"
p:JSimpleDB-ref="jsimpledb" p:allowNewSchema="true"/>

<!-- Enable @Transactional annotations -->
<tx:annotation-driven transaction-manager="transactionManager"/>


Unknown end tag for &lt;/beans&gt;

```

Of course, you can construct the equivalent `JSimpleDB` instance in plain Java as well:

```java

final JSimpleDB jdb = new JSimpleDBFactory()
.setDatabase(new Database(new XMLKVDatabase(new File("/tmp/jsimpledb.xml"))))
.setSchemaVersion(1)
.setModelClasses(MyClass1.class, MyClass2.class, ... )
.newJSimpleDB();```

## Creating Transactions ##

Once you have a `JSimpleDB` instance, you can create and commit transactions:

```java

final JTransaction jtx = jdb.createTransaction(true, ValidationMode.AUTOMATIC);
JTransaction.setCurrent(jtx);
try {
// Do work here ...
tx.commit();
} finally {
JTransaction.setCurrent(null);
}```

When using the Spring transaction manager, this can be simplified:

```java

@Transactional
public void doSomething() {
// Do work here ...
}```

Like most databases, the underlying key/value store is allowed to throw an exception if it detects a deadlock or unresolvable lock conflict. In the case of `JSimpleDB`, the exception thrown is a `RetryTransactionException`; the JSimpleDB Spring transaction manager converts this into a `PessimisticLockingFailureException`.

In these situations the transaction should be retried. The [DellRoad Stuff](https://code.google.com/p/dellroad-stuff/) library provides the [@RetryTransaction](http://dellroad-stuff.googlecode.com/svn/trunk/publish/reports/javadoc/index.html?org/dellroad/stuff/spring/RetryTransaction.html) annotation that automatically retries transactions:

```java

@RetryTransaction
@Transactional
public void doSomething() {
// Do work here ...
}```

## Hello World ##

Here's a complete example using normal Java code that uses a flat file `jsimpledb.xml` for persistence:

```java

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.NavigableMap;
import java.util.Set;

import org.jsimpledb.JObject;
import org.jsimpledb.JSimpleDB;
import org.jsimpledb.JSimpleDBFactory;
import org.jsimpledb.JTransaction;
import org.jsimpledb.ValidationMode;
import org.jsimpledb.annotation.JSimpleClass;
import org.jsimpledb.core.Database;
import org.jsimpledb.kv.simple.XMLKVDatabase;

public class JSimpleDBExample {

public static void main(String[] args) throws Exception {

// Create database
final JSimpleDB jdb = new JSimpleDBFactory()
.setDatabase(new Database(new XMLKVDatabase(new File("jsimpledb.xml"))))
.setSchemaVersion(1)
.setModelClasses(Person.class)
.newJSimpleDB();

// Write to database
JTransaction jtx = jdb.createTransaction(true, ValidationMode.AUTOMATIC);
JTransaction.setCurrent(jtx);
try {

System.out.println("Writing database...");

Person fred = Person.create();
Person joe = Person.create();
Person sally = Person.create();

fred.setName("Fred");
fred.setAge(25);

joe.setName("Joe");
joe.setAge(35);

sally.setName("Sally");
sally.setAge(45);

fred.getFriends().add(joe);
fred.getFriends().add(sally);

joe.getFriends().add(sally);

sally.getFriends().add(fred);

jtx.commit();
} finally {
JTransaction.setCurrent(null);
}

// Read from database
jtx = jdb.createTransaction(true, ValidationMode.AUTOMATIC);
JTransaction.setCurrent(jtx);
try {

System.out.println("Reading database...");

for (Person person : Person.getAll())
System.out.println(person);

jtx.rollback();
} finally {
JTransaction.setCurrent(null);
}
}

// Model Classes

@JSimpleClass
public abstract static class Person implements JObject {

// My age
public abstract int getAge();
public abstract void setAge(int age);

// My name
public abstract String getName();
public abstract void setName(String name);

// My friends
public abstract Set<Person> getFriends();

// Inverse of friends
public Set<Person> getFriendsToMe() {
Set<Person> set = this.getTransaction().queryIndex(
Person.class, "friends.element", Person.class).asMap().get(this);
return set != null ? set : Collections.<Person>emptySet();
}

@Override
public String toString() {
final StringBuilder buf = new StringBuilder();
buf.append("Person (id " + this.getObjId() + ")\n");
buf.append("  Name: " + this.getName() + "\n");
buf.append("  Age: " + this.getAge() + "\n");
buf.append("  ->Friends of " + this.getName() + ":\n");
for (Person friend : this.getFriends())
buf.append("    " + friend.getName() + "\n");
buf.append("  <-Friends with " + this.getName() + ":\n");
for (Person reverseFriend : this.getFriendsToMe())
buf.append("    " + reverseFriend.getName() + "\n");
return buf.toString().trim();
}

public static Person create() {
return JTransaction.getCurrent().create(Person.class);
}

public static NavigableSet<Person> getAll() {
return JTransaction.getCurrent().getAll(Person.class);
}
}
}```

and sample output:
```
Writing database...
Reading database...
Person (id fcc572e098b9b256)
  Name: Joe
  Age: 35
  ->Friends of Joe:
    Sally
  <-Friends with Joe:
    Fred
Person (id fcc572b4739d0e5e)
  Name: Sally
  Age: 45
  ->Friends of Sally:
    Fred
  <-Friends with Sally:
    Joe
    Fred
Person (id fcc57229bf773c29)
  Name: Fred
  Age: 25
  ->Friends of Fred:
    Joe
    Sally
  <-Friends with Fred:
    Sally
```

To use [LevelDB](http://jsimpledb.googlecode.com/svn/trunk/publish/reports/javadoc/index.html?org/jsimpledb/kv/leveldb/LevelDBKVDatabase.html) as the key/value store, replace the first line of the `main()` method above with:

```java

final LevelDBKVDatabase ldb = new LevelDBKVDatabase();
ldb.setDirectory("/tmp/test");
ldb.start();
final Database coreDb = new Database(ldb);```