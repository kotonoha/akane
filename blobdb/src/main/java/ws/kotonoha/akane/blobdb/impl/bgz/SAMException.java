package ws.kotonoha.akane.blobdb.impl.bgz;

/**
 * @author eiennohito
 * @since 2014-08-15
 */
public class SAMException extends RuntimeException {
  public SAMException() {
    super();
  }

  public SAMException(String message) {
    super(message);
  }

  public SAMException(String message, Throwable cause) {
    super(message, cause);
  }

  public SAMException(Throwable cause) {
    super(cause);
  }

  protected SAMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
