
package fr.cls.argos.dataxmldistribution.service;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "DixException", targetNamespace = "http://service.dataxmldistribution.argos.cls.fr/types")
public class DixException
    extends Exception
{

    private static final long serialVersionUID = 9060885496428939201L;
    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private fr.cls.argos.dataxmldistribution.service.types.DixException faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public DixException(String message, fr.cls.argos.dataxmldistribution.service.types.DixException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public DixException(String message, fr.cls.argos.dataxmldistribution.service.types.DixException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: fr.cls.argos.dataxmldistribution.service.types.DixException
     */
    public fr.cls.argos.dataxmldistribution.service.types.DixException getFaultInfo() {
        return faultInfo;
    }

}
