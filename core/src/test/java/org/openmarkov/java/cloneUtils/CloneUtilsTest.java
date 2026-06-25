package org.openmarkov.java.cloneUtils;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CloneUtilsTest {
    
    @Test
    void testSafeCloneForNull() {
        assertNull(CloneUtils.safeClone(null));
    }
    
    @Test
    void testSafeCloneForCustomClass() {
        ACloneableClass originalUserHolder = new ACloneableClass(new User("Jorge", 25));
        ACloneableClass clonedUserHolder = CloneUtils.safeClone(originalUserHolder);
        assertNotSame(originalUserHolder, clonedUserHolder);
        assertNotSame(originalUserHolder.user, clonedUserHolder.user);
        assertEquals(originalUserHolder, clonedUserHolder);
        assertEquals(originalUserHolder.user, clonedUserHolder.user);
    }
    
    private static final List<User> USER_LIST = List.of(
            new User("Alba", 21),
            new User("Elena", 22),
            new User("Irina", 23),
            new User("Olivia", 24),
            new User("Úrsula", 25)
    );
    
    @Test
    void testSafeCloneForArrayList() {
        ArrayList<User> users = new ArrayList<>(USER_LIST);
        ArrayList<User> clonedUsers = CloneUtils.safeClone(users);
        
        assertNotSame(users, clonedUsers);
        assertEquals(users, clonedUsers);
        for (int i = 0; i < users.size(); i++) {
            assertNotSame(users.get(i), clonedUsers.get(i));
            assertEquals(users.get(i), clonedUsers.get(i));
        }
    }
    
    @Test
    void testSafeCloneForHashMap() {
        HashMap<String, User> usersByFirstLetter = new HashMap<>();
        for (User user : USER_LIST) {
            usersByFirstLetter.put(user.name.substring(0, 1), user);
        }
        HashMap<String, User> clonedUsersByFirstLetter = CloneUtils.safeClone(usersByFirstLetter);
        
        assertNotSame(usersByFirstLetter, clonedUsersByFirstLetter);
        assertEquals(usersByFirstLetter, clonedUsersByFirstLetter);
        for (String firstLetter : usersByFirstLetter.keySet()) {
            assertNotSame(usersByFirstLetter.get(firstLetter), clonedUsersByFirstLetter.get(firstLetter));
            assertEquals(usersByFirstLetter.get(firstLetter), clonedUsersByFirstLetter.get(firstLetter));
        }
    }
    
    record ACloneableClass(User user) implements Cloneable {
        @Override public ACloneableClass clone() {
            return new ACloneableClass(CloneUtils.safeClone(this.user));
        }
    }
    
    record User(String name, Integer age) implements Cloneable {
        @Override public User clone() {
            return new User(this.name, this.age);
        }
    }
}