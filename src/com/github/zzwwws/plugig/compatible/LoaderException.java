package com.github.zzwwws.plugig.compatible;

final class LoaderException extends Exception {
	private static final long serialVersionUID = 484958029591916285L;

    public LoaderException() {
    }

    public LoaderException(String detailMessage) {
        super(detailMessage);
    }

    public LoaderException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public LoaderException(Throwable throwable) {
        super(throwable);
    }
}