/**
 *
 */
package com.slimgears.rxrpc.apt.data;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public interface HasType {
    TypeInfo type();

    interface Builder<B extends Builder<B>> {
        B type(TypeInfo type);

        default B type(TypeMirror typeMirror) {
            return type(TypeInfo.of(typeMirror));
        }

        default B type(TypeElement typeElement) {
            return type(TypeInfo.of(typeElement));
        }
    }
}
