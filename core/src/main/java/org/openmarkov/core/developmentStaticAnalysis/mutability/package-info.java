/**
 * <h2>Immutability</h2>
 * An Immutability object is one whose state cannot be changed by any means. To meet this in the Object-Oriented
 * Paradigm this there are properties that must be met: Exterior mutability and Interior mutability.
 *
 * <h2>Exterior Immutability</h2>
 * An Exterior Immutable object is an object whose fields cannot be changed/reassigned after it is created.
 * <p>
 * In Java, this means the fields of an exterior immutable class must be {@code final}, and therefore they <b>CANNOT</b>
 * change.
 * <p>
 * <h2>Interior Immutability</h2>
 * An Interior Immutable object is an object whose fields cannot change internally.
 * <p>
 * This is met when the classes of the fields are both exterior and interior immutable (In simpler words, when the
 * fields of those fields' classes are {@code final} recursively).
 *
 *
 * <h2>Comparasion of mutabilities</h2>
 * <pre>
 * {@code
 *  class Human{
 *      final String name;
 *      int age;
 *      final ArrayList<Human> relatives;
 *  }
 * }
 * </pre>
 * <p>
 * The previous class contains three parameters:
 *
 * <table>
 *     <caption>Comparison of exterior and interior mutability for each field</caption>
 *     <tr>
 *         <td></td>
 *         <td>Exterior mutability</td>
 *         <td>Interior mutability</td>
 *     </tr>
 *     <tr>
 *         <td>{@code final String name}</td>
 *         <td>
 *              Immutable: It is declared {@code final}, meaning it is exterior immutable as we cannot reassign the
 *              value of {@code name} after it is created.
 *              <br><br>
 *              This means something like {@code this.name="Jorge"} won't compile, unless if written in the constructor.
 *         </td>
 *         <td>
 *              Immutable: The contents of a {@code String} cannot change, meaning it is interior immutable as we
 *              cannot modify the {@code String} in any way.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code int age}</td>
 *         <td>
 *              Mutable: It is not declared {@code final}, meaning it is exterior mutable as we can reassign the
 *              value of {@code age}, such as if someone's birthday arrives, (This means something like
 *              {@code this.age=25} is valid everywhere).
 *         </td>
 *         <td>
 *              Immutable: The contents of an {@code int} cannot change, meaning it is <b>interior immutable</b> as we
 *              cannot modify the {@code int} in any way.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code final ArrayList<Human> relatives}</td>
 *         <td>
 *              Immutable: It is declared {@code final}, meaning it is <b>exterior immutable</b> as we cannot reassign
 *              the value of {@code relatives} after it is created.
 *              <br><br>
 *              This means something like {@code this.relatives=new ArrayList()} won't compile, unless if written in the
 *              constructor.
 *         </td>
 *         <td>
 *              Mutable: The contents of an {@code ArrayList} CAN change. Even if the field {@code this.relatives} is
 *              {@code final}, we can write {@code this.relatives.add(newRelative)} and therefore we would modify the
 *              contents of the {@code ArrayList}, meaning it is <b>interior mutable</b> as the contents of the
 *              {@code ArrayList} can change at anytime.
 *         </td>
 *     </tr>
 * </table>
 *
 * <h2>Checking mutability in OpenMarkov</h2>
 * OpenMarkov offers two interfaces named {@link org.openmarkov.core.developmentStaticAnalysis.mutability.InteriorImmutable} and
 * {@link org.openmarkov.core.developmentStaticAnalysis.mutability.ExteriorImmutable} to represent both kinds of immutability. If you
 * implement any of them in a class, then your class mutability will be checked in an Integration Test.
 * <p>
 * If a field makes so a class cannot be considered mutable, then the test will output which fields are causing said
 * problem.
 */
package org.openmarkov.core.developmentStaticAnalysis.mutability;