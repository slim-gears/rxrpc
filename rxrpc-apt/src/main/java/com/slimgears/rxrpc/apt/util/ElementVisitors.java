package com.slimgears.rxrpc.apt.util;

import javax.lang.model.element.*;
import javax.lang.model.util.AbstractElementVisitor8;
import java.util.function.Consumer;

public class ElementVisitors {
    public static Builder visitorBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Consumer<PackageElement> onPackage = doNothing();
        private Consumer<TypeElement> onType = doNothing();
        private Consumer<VariableElement> onVariable = doNothing();
        private Consumer<ExecutableElement> onExecutable = doNothing();
        private Consumer<TypeParameterElement> onTypeParameter = doNothing();

        public Builder onPackage(Consumer<PackageElement> consumer) {
            this.onPackage = this.onPackage.andThen(consumer);
            return this;
        }

        public Builder onType(Consumer<TypeElement> consumer) {
            this.onType = this.onType.andThen(consumer);
            return this;
        }

        public Builder onVariable(Consumer<VariableElement> consumer) {
            this.onVariable = this.onVariable.andThen(consumer);
            return this;
        }

        public Builder onExecutable(Consumer<ExecutableElement> consumer) {
            this.onExecutable = this.onExecutable.andThen(consumer);
            return this;
        }

        public Builder onTypeParameter(Consumer<TypeParameterElement> consumer) {
            this.onTypeParameter = this.onTypeParameter.andThen(consumer);
            return this;
        }

        public ElementVisitor<Void, Void> build() {
            return new AbstractElementVisitor8<Void, Void>() {
                @Override
                public Void visitPackage(PackageElement e, Void aVoid) {
                    onPackage.accept(e);
                    return null;
                }

                @Override
                public Void visitType(TypeElement e, Void aVoid) {
                    onType.accept(e);
                    return null;
                }

                @Override
                public Void visitVariable(VariableElement e, Void aVoid) {
                    onVariable.accept(e);
                    return null;
                }

                @Override
                public Void visitExecutable(ExecutableElement e, Void aVoid) {
                    onExecutable.accept(e);
                    return null;
                }

                @Override
                public Void visitTypeParameter(TypeParameterElement e, Void aVoid) {
                    onTypeParameter.accept(e);
                    return null;
                }
            };
        }

        public void traverse(Element element) {
            element.accept(build(), null);
        }
    }

    private static <T> Consumer<T> doNothing() {
        return val -> {};
    }
}
