/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

public class StateTest {
    
    // TODO Sobrecargar equals y poner en el comentario que
    // equals ya NO consiste en comparar la dirección de memoria de dos objetos
    // TODO Cada tipo de State tiene que tener un método equals y llamar al del padre
    public static boolean equalStates(State state1, State state2) {
        return state1.getName().contentEquals(state2.getName());
    }
    
}
