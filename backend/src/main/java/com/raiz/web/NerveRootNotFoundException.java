package com.raiz.web;

class NerveRootNotFoundException extends RuntimeException {

    NerveRootNotFoundException(String code) {
        super("No existe la raíz " + code + " en el catálogo.");
    }
}
