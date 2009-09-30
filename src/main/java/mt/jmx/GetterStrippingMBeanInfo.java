package mt.jmx;


import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import java.util.ArrayList;
import java.util.List;

public class GetterStrippingMBeanInfo
        extends ModelMBeanInfoSupport
{
    public GetterStrippingMBeanInfo(ModelMBeanInfo info)
    {
        super(info);
    }

    @Override
    public GetterStrippingMBeanInfo clone()
    {
        return new GetterStrippingMBeanInfo(this);
    }

    /**
     * Get rid of getter and setter operations as the mbeaninfo is serialized
     * Clients will see just the attributes
     * 
     * @return
     */
    private Object writeReplace()
    {
        List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();

        for (MBeanOperationInfo operation : getOperations()) {
            Descriptor descriptor = operation.getDescriptor();
            String role = (String) descriptor.getFieldValue("role");
            if (!"getter".equalsIgnoreCase(role) && !"setter".equalsIgnoreCase(role)) {
                operations.add(operation);
            }
        }

        try {
            return new ModelMBeanInfoSupport(
                    this.getClassName(),
                    this.getDescription(),
                    (ModelMBeanAttributeInfo[]) this.getAttributes(),
                    (ModelMBeanConstructorInfo[]) this.getConstructors(),
                    operations.toArray(new ModelMBeanOperationInfo[0]),
                    (ModelMBeanNotificationInfo[]) this.getNotifications(),
                    this.getMBeanDescriptor());
        }
        catch (MBeanException e) {
            throw new RuntimeException(e);
        }
    }
}
