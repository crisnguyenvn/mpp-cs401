package midterm.data;

import midterm.entity.Book;
import midterm.entity.BookCopy;
import midterm.entity.CheckoutRecord;
import midterm.entity.LibraryMember;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;


public class DataAccessFacade implements DataAccess {

    enum StorageType {
        BOOKS, MEMBERS, USERS, CHECKOUTRECORD
    }

    public static final String OUTPUT_DIR = System.getProperty("user.dir")
            + "/src/midterm/data/storage";
    public static final String DATE_PATTERN = "MM/dd/yyyy";

    // implement: other save operations
    public void saveNewMember(LibraryMember member) {
        HashMap<String, LibraryMember> mems = readMemberMap();
        if (mems != null) {
            String memberId = member.getMemberId();
            mems.put(memberId, member);
            saveToStorage(StorageType.MEMBERS, mems);
        }
    }

    public HashMap<String, CheckoutRecord> saveNewCheckoutRecord(CheckoutRecord checkoutRecord, int copyNum) {
        HashMap<String, CheckoutRecord> checkoutRecordHashMap = readCheckoutRecordMap();
        if (checkoutRecordHashMap == null) {
            checkoutRecordHashMap = new HashMap<>();
        }
        String memberId = checkoutRecord.getLibraryMember().getMemberId() + copyNum;
        checkoutRecordHashMap.put(memberId, checkoutRecord);
        saveToStorage(StorageType.CHECKOUTRECORD, checkoutRecordHashMap);
        return checkoutRecordHashMap;
    }

    @Override
    public void updateBookCopyAvailability(String isbn, BookCopy checkoutBookCopy) {
        HashMap<String, Book> books = readBooksMap();
        BookCopy[] bookCopies = books.get(isbn).getCopies();
        int newCopyNum = checkoutBookCopy.getCopyNum();

        for (BookCopy bookCopy : bookCopies) {
            if (bookCopy.getCopyNum() == newCopyNum) {
                bookCopy.changeAvailability();
            }
        }
        saveToStorage(StorageType.BOOKS, books);
    }

    @Override
    public LibraryMember searchMember(String memberId) {
        HashMap<String, LibraryMember> members = (HashMap<String, LibraryMember>) readFromStorage(StorageType.MEMBERS);
        if (members != null) {
            return members.get(memberId);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, CheckoutRecord> readCheckoutRecordMap() {
        return (HashMap<String, CheckoutRecord>) readFromStorage(StorageType.CHECKOUTRECORD);
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Book> readBooksMap() {
        //Returns a Map with name/value pairs being
        //   isbn -> Book
        return (HashMap<String, Book>) readFromStorage(StorageType.BOOKS);
    }

    @Override
    public Book searchBook(String isbn) {
        HashMap<String, Book> books = (HashMap<String, Book>) readFromStorage(StorageType.BOOKS);
        return books.get(isbn);
    }

    @Override
    public void saveBook(Book book) {
        HashMap<String, Book> books = readBooksMap();
        if (books != null) {
            String bookisbn = book.getIsbn();
            books.put(bookisbn, book);
            saveToStorage(StorageType.BOOKS, books);
        }
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, LibraryMember> readMemberMap() {
        //Returns a Map with name/value pairs being
        //   memberId -> LibraryMember
        return (HashMap<String, LibraryMember>) readFromStorage(
                StorageType.MEMBERS);
    }

    @Override
    public void updateNewBook(Book book) {
        HashMap<String, Book> books = readBooksMap();
        String isbn = book.getIsbn();

        if (books.containsKey(isbn)) {
            books.put(isbn, book);
            saveToStorage(StorageType.BOOKS, books);
        }
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, User> readUserMap() {
        //Returns a Map with name/value pairs being
        //   userId -> User
        return (HashMap<String, User>) readFromStorage(StorageType.USERS);
    }


    static void saveToStorage(StorageType type, HashMap<? extends String, ? extends Serializable> ob) {
        ObjectOutputStream out = null;
        try {
            Path path = FileSystems.getDefault().getPath(OUTPUT_DIR, type.toString());
            out = new ObjectOutputStream(Files.newOutputStream(path));
            out.writeObject(ob);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static Object readFromStorage(StorageType type) {
        ObjectInputStream in = null;
        Object retVal = null;
        try {
            Path path = FileSystems.getDefault().getPath(OUTPUT_DIR, type.toString());
            in = new ObjectInputStream(Files.newInputStream(path));
            retVal = in.readObject();
        } catch (Exception e) {
//			e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
        return retVal;
    }

    static void loadBookMap(List<Book> bookList) {
        HashMap<String, Book> books = new HashMap<String, Book>();
        bookList.forEach(book -> books.put(book.getIsbn(), book));
        saveToStorage(StorageType.BOOKS, books);
    }

    static void loadUserMap(List<User> userList) {
        HashMap<String, User> users = new HashMap<String, User>();
        userList.forEach(user -> users.put(user.getId(), user));
        saveToStorage(StorageType.USERS, users);
    }

    static void loadMemberMap(List<LibraryMember> memberList) {
        HashMap<String, LibraryMember> members = new HashMap<String, LibraryMember>();
        memberList.forEach(member -> members.put(member.getMemberId(), member));
        saveToStorage(StorageType.MEMBERS, members);
    }

    final static class Pair<S, T> implements Serializable {

        S first;
        T second;

        Pair(S s, T t) {
            first = s;
            second = t;
        }

        @Override
        public boolean equals(Object ob) {
            if (ob == null) return false;
            if (this == ob) return true;
            if (ob.getClass() != getClass()) return false;
            @SuppressWarnings("unchecked")
            Pair<S, T> p = (Pair<S, T>) ob;
            return p.first.equals(first) && p.second.equals(second);
        }

        @Override
        public int hashCode() {
            return first.hashCode() + 5 * second.hashCode();
        }

        @Override
        public String toString() {
            return "(" + first.toString() + ", " + second.toString() + ")";
        }

        private static final long serialVersionUID = 5399827794066637059L;
    }

}
