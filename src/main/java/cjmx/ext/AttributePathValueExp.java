package cjmx.ext;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryEval;
import javax.management.ReflectionException;
import javax.management.ValueExp;
import javax.management.openmbean.CompositeData;


/**
 * Value expression that supports JMX monitor syntax, as described in the JavaDoc of
 * the {@link javax.management.monitor} package.
 *
 * <p>This allows referencing nested values in queries.  For example, {@code HeapMemoryUsage.used}.
 * A path to a value is specified by an attribute name and zero or more path segments, where each
 * path segment represents the name of a value nested in the previous value or attribute value in the
 * case of the first path segment.</p>
 *
 * <p>A path segment is resolved against a value in the following way:
 *  <ul>
 *   <li>If the value is a {@link CompositeData}, the path segment is used as the key name in a lookup to the {@code CompositeData} and the resulting value is returned.</li>
 *   <li>If the value is an array and the path segment is the string {@code length}, the length of the array is returned.</li>
 *   <li>If the value is a Java Bean, the path segment is used as a property name and the property's read method is invoked and resulting value is returned.</li>
 *  </ul>
 * </p>
 *
 * This process is repeated recursively for each path segment until there are no path segments remaining, at which point the last produced value is returned wrapped in a
 * literal value expression.
 */
public final class AttributePathValueExp implements ValueExp {

    private final String attributeName;
    private final List<String> path;

    /**
     * Creates an expression.
     *
     * @param attributeName name of the attribute
     * @param path ordered collection of path segments
     */
    public AttributePathValueExp(final String attributeName, final Collection<String> path) {
        assert attributeName != null : "attributeName must not be null";
        this.attributeName = attributeName;
        this.path = new ArrayList<String>(path);
    }

    @Override
    public ValueExp apply(final ObjectName name) {
        final MBeanServer server = QueryEval.getMBeanServer();
        try {
            final Object attrValue = server.getAttribute(name, attributeName);
            return getValue(attrValue, path.iterator());
        } catch (final MBeanException e) {
            throw new RuntimeException(e);
        } catch (final AttributeNotFoundException e) {
            throw new RuntimeException(e);
        } catch (final InstanceNotFoundException e) {
            throw new RuntimeException(e);
        } catch (final ReflectionException e) {
            throw new RuntimeException(e);
        }
    }

    private static ValueExp getValue(final Object value, final Iterator<String> path) {
        if (path.hasNext()) {
            final String segment = path.next();
            if (value instanceof CompositeData) {
                final Object segmentValue = ((CompositeData)value).get(segment);
                return getValue(segmentValue, path);
            } else if (value instanceof Object[]) {
                return getValue(((Object[])value).length, path);
            } else if (value instanceof byte[]) {
                return getValue(((byte[])value).length, path);
            } else if (value instanceof char[]) {
                return getValue(((char[])value).length, path);
            } else if (value instanceof double[]) {
                return getValue(((double[])value).length, path);
            } else if (value instanceof float[]) {
                return getValue(((float[])value).length, path);
            } else if (value instanceof int[]) {
                return getValue(((int[])value).length, path);
            } else if (value instanceof long[]) {
                return getValue(((long[])value).length, path);
            } else if (value instanceof short[]) {
                return getValue(((short[])value).length, path);
            } else {
                return getValueViaIntrospection(value, path, segment);
            }
        } else {
            return asValueExp(value);
        }
    }

    private static ValueExp getValueViaIntrospection(final Object value, final Iterator<String> path, final String segment) {
        try {
            final BeanInfo info = Introspector.getBeanInfo(value.getClass());
            PropertyDescriptor pdesc = null;
            for (final PropertyDescriptor pd : info.getPropertyDescriptors()) {
                if (pd.getName().equalsIgnoreCase(segment))
                    pdesc = pd;
            }
            if (pdesc == null) {
                throw newUnsupportedValue(value, segment);
            } else {
                final Method getter = pdesc.getReadMethod();
                try {
                    getter.setAccessible(true);
                } catch (final SecurityException e) {
                    // Ignore; may not have permission to do this
                }
                final Object segmentValue = getter.invoke(value);
                return getValue(segmentValue, path);
            }
        } catch (final IntrospectionException e) {
            throw newUnsupportedValue(value, segment);
        } catch (final IllegalAccessException e) {
            throw newUnsupportedValue(value, segment);
        } catch (final InvocationTargetException e) {
            throw newUnsupportedValue(value, segment);
        }
    }

    private static RuntimeException newUnsupportedValue(final Object value, final String segment) {
        return new RuntimeException(String.format("Unsupported intermediate value [%s] while processing path segment [%s].", value, segment));
    }

    private static ValueExp asValueExp(final Object value) {
        if (value instanceof Boolean)
            return Query.value((Boolean)value);
        else if (value instanceof Double)
            return Query.value((Double)value);
        else if (value instanceof Float)
            return Query.value((Float)value);
        else if (value instanceof Integer)
            return Query.value((Integer)value);
        else if (value instanceof Long)
            return Query.value((Long)value);
        else if (value instanceof Number)
            return Query.value((Number)value);
        else if (value instanceof String)
            return Query.value((String)value);
        else
            return Query.value(value.toString());
    }

    @Override
    public void setMBeanServer(final MBeanServer svr) {
    }
}

