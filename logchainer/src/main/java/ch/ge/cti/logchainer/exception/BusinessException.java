package ch.ge.cti.logchainer.exception;

/**
 * Exception de base utilis�e pour traiter les erreurs m�tiers.
 */
@SuppressWarnings("serial")
public class BusinessException extends RuntimeException {
//    private static final long serialVersionUID = 6088765962073071589L;

    private final String fieldInError;
    private final String messageKey;
    private final Object[] parameters;

    /**
     * @param messageKey   cl� du message r�solvable par le bean messageSource
     * @param fieldInError nom de l'attribut en erreur
     * @param parameters   liste des param�tres associ�s au message
     */
    public BusinessException(String messageKey, String fieldInError, Object... parameters) {
	super();
	this.messageKey = messageKey;
	this.fieldInError = fieldInError;
	this.parameters = parameters;
    }

    /**
     * @param messageKey   cl� du message r�solvable par le bean messageSource
     * @param fieldInError nom de l'attribut en erreur
     * @param cause        cause de l'erreur de validation si provoqu�e par une exception
     * @param parameters   liste des param�tres associ�s au message
     */
    public BusinessException(String messageKey, String fieldInError, Throwable cause, Object... parameters) {
	super(cause);
	this.messageKey = messageKey;
	this.fieldInError = fieldInError;
	this.parameters = parameters;
    }

    /**
     * @param messageKey cl� du message r�solvable par le bean messageSource
     */
    public BusinessException(String messageKey) {
	this(messageKey, "");
    }

    /**
     * @param messageKey cl� du message r�solvable par le bean messageSource
     * @param parameters liste des param�tres associ�s au message
     */
    public BusinessException(String messageKey, Object... parameters) {
	this(messageKey, "", parameters);
    }
    
    public BusinessException(Throwable cause) {
	this("", "", cause);
    }
    
    public BusinessException(String message, Throwable cause) {
	this(message, "", cause);
    }
    
    public BusinessException(String messageKey, Throwable cause, Object... parameters) {
	this(messageKey, "", cause, parameters);
    }

    public String getFieldInError() {
	return fieldInError;
    }

    public String getMessageKey() {
	return messageKey;
    }

    public Object[] getParameters() {
	return parameters;
    }
}
