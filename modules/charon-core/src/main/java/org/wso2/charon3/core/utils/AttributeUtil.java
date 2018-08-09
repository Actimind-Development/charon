/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.charon3.core.utils;

import org.json.JSONObject;
import org.wso2.charon3.core.attributes.Attribute;
import org.wso2.charon3.core.attributes.ComplexAttribute;
import org.wso2.charon3.core.attributes.DefaultAttributeFactory;
import org.wso2.charon3.core.attributes.MultiValuedAttribute;
import org.wso2.charon3.core.attributes.SimpleAttribute;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.objects.AbstractSCIMObject;
import org.wso2.charon3.core.protocol.ResponseCodeConstants;
import org.wso2.charon3.core.schema.AttributeSchema;
import org.wso2.charon3.core.schema.SCIMAttributeSchema;
import org.wso2.charon3.core.schema.SCIMConstants;
import org.wso2.charon3.core.schema.SCIMDefinitions;
import org.wso2.charon3.core.schema.SCIMResourceTypeSchema;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This class acts as an utility class for attributes.
 */
public class AttributeUtil {

    /*
     * Convert the raw string to SCIM defined data type accordingly
     *
     * @param attributeValue
     * @param dataType
     * @return Object
     */
    public static Object getAttributeValueFromString(Object attributeValue,
                                                     SCIMDefinitions.DataType dataType)
            throws CharonException, BadRequestException {
        if (attributeValue == null) {
            return attributeValue;
        }
        String attributeStringValue = null;
        if (attributeValue instanceof Boolean) {
            attributeStringValue = String.valueOf(attributeValue);
        } else if (attributeValue instanceof Integer) {
            attributeStringValue = String.valueOf(attributeValue);
        } else if (attributeValue instanceof Double) {
            attributeStringValue = String.valueOf(attributeValue);
        } else if (JSONObject.NULL.equals(attributeValue)) {
            attributeStringValue = "";
        } else {
            attributeStringValue = (String) attributeValue;
        }

        switch (dataType) {
            case STRING:
                return attributeStringValue.trim();
            case BOOLEAN:
                return parseBoolean(attributeValue);
            case DECIMAL:
                return Double.parseDouble(attributeStringValue);
            case INTEGER:
                return Integer.parseInt(attributeStringValue);
            case DATE_TIME:
                return parseDateTime(attributeStringValue);
            case BINARY:
                return Byte.valueOf(attributeStringValue);
            case REFERENCE:
                return parseReference(attributeStringValue);
            case COMPLEX:
                return parseComplex(attributeStringValue);
        }
        return null;
    }

    /*
     * return the string value of the attribute value
     *
     * @param attributeValue
     * @param dataType
     * @return
     * @throws CharonException
     */
    public static String getStringValueOfAttribute(Object attributeValue,
                                                   SCIMDefinitions.DataType dataType)
            throws CharonException {
        switch (dataType) {
            case STRING:
                return String.valueOf(attributeValue);
            case BOOLEAN:
                return String.valueOf(attributeValue);
            case DECIMAL:
                return String.valueOf(attributeValue);
            case INTEGER:
                return String.valueOf(attributeValue);
            case DATE_TIME:
                return formatDateTime((Date) attributeValue);
            case BINARY:
                return String.valueOf(attributeValue);
            case REFERENCE:
                return String.valueOf(attributeValue);
            case COMPLEX:
                return String.valueOf(attributeValue);
        }
        throw new CharonException("Error in converting attribute value of type: " + dataType + " to string.");
    }

    /*
     * SCIM spec requires date time to be in yyyy-MM-dd'T'HH:mm:ss
     *
     * @param dateTimeString
     */
    public static Date parseDateTime(String dateTimeString) throws CharonException {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(SCIMConstants.DATE_TIME_FORMAT);
            return sdf.parse(dateTimeString);
        } catch (ParseException e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(SCIMConstants.DATE_TIME_FORMAT2);
                return sdf.parse(dateTimeString);
            } catch (ParseException e1) {
                throw new CharonException("Error in parsing date time. " +
                        "Date time should adhere to the format: " + SCIMConstants.DATE_TIME_FORMAT, e1);
            }
        }
    }

    public static String parseReference(String referenceString) throws CharonException {
        //TODO: Need a better way for doing this. Think of the way to handle reference types
        return referenceString;
    }

    //this method is for the consistency purpose only
    public static String parseComplex(String complexString) {
        return complexString;
    }

    /*
     * SCIM spec requires date time to be adhered to XML Schema Datatypes Specification
     *
     * @param date
     */
    public static String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(SCIMConstants.DATE_TIME_FORMAT);
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    /*
     * Converts the value to boolean or throw an exception
     *
     * @param booleanValue
     */
    public static Boolean parseBoolean(Object booleanValue) throws BadRequestException {
        try {
            return ((Boolean) booleanValue);
        } catch (Exception e) {
            return Boolean.parseBoolean((String) booleanValue);
        }
    }

    /*
     * Will iterate through <code>{@code SCIMAttributeSchema}</code> objects
     *
     * @param attributeName
     * @return
     */
    public static String getAttributeURI(String attributeName, SCIMResourceTypeSchema schema) throws
            BadRequestException {

        Iterator<AttributeSchema> attributeSchemas = schema.getAttributesList().iterator();
        while (attributeSchemas.hasNext()) {
            AttributeSchema attributeSchema = attributeSchemas.next();

            if (attributeSchema.getName().equalsIgnoreCase(attributeName) || attributeSchema.getURI().equals
                    (attributeName)) {
                return attributeSchema.getURI();
            }
            // check in sub attributes
            String subAttributeURI =
                    checkSCIMSubAttributeURIs(((SCIMAttributeSchema) attributeSchema).getSubAttributeSchemas(),
                            attributeSchema, attributeName);
            if (subAttributeURI != null) {
                return subAttributeURI;
            }

            if (attributeName.contains(attributeSchema.getName()) && attributeSchema.getMultiValued()) {

                String subAttribute = null;
                if (attributeName.contains(".")) {
                    String[] splittedString = attributeName.split("\\.", 2);
                    subAttribute = splittedString[1];
                }
                subAttributeURI = attributeSchema.getURI();
                if (subAttribute != null) {
                    subAttributeURI = subAttributeURI + "." + subAttribute;
                    return subAttributeURI;
                }
            }
        }
        String error = "Not a valid attribute name/uri";
        throw new BadRequestException(error, ResponseCodeConstants.INVALID_VALUE);
    }

    /*
     * Will iterate through <code>{@code SCIMSubAttributeSchema}</code> objects
     *
     * @param subAttributes
     * @param attributeSchema
     * @param attributeName   @return
     */
    private static String checkSCIMSubAttributeURIs(List<SCIMAttributeSchema> subAttributes,
                                                    AttributeSchema attributeSchema, String attributeName) {
        if (subAttributes != null) {
            Iterator<SCIMAttributeSchema> subsIterator = subAttributes.iterator();

            while (subsIterator.hasNext()) {
                SCIMAttributeSchema subAttributeSchema = subsIterator.next();
                if ((attributeSchema.getName() + "." + subAttributeSchema.getName()).equalsIgnoreCase(attributeName) ||
                        subAttributeSchema.getURI().equals(attributeName)) {
                    return subAttributeSchema.getURI();
                }
                if (subAttributeSchema.getType().equals(SCIMDefinitions.DataType.COMPLEX)) {
                    List<SCIMAttributeSchema> subSubAttributeSchemas = subAttributeSchema.getSubAttributeSchemas();
                    if (subSubAttributeSchemas != null) {
                        Iterator<SCIMAttributeSchema> subSubsIterator = subSubAttributeSchemas.iterator();

                        while (subSubsIterator.hasNext()) {
                            SCIMAttributeSchema subSubAttributeSchema = subSubsIterator.next();
                            if ((attributeSchema.getName() + "." + subAttributeSchema.getName() + "." +
                                    subSubAttributeSchema.getName()).equalsIgnoreCase(attributeName) ||
                                    subAttributeSchema.getURI().equals(attributeName)) {
                                return subSubAttributeSchema.getURI();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static SimpleAttribute simple(AttributeSchema schema, Object value)
            throws BadRequestException, CharonException {
        if (schema.getMultiValued() || schema.getType() == SCIMDefinitions.DataType.COMPLEX) {
            throw new CharonException("Shall be called for simple attributes only");
        }
        SimpleAttribute result = (SimpleAttribute) DefaultAttributeFactory.createAttribute(schema,
                new SimpleAttribute(schema.getName(), value));
        result.setType(schema.getType());
        return result;
    }

    public static MultiValuedAttribute strings(AttributeSchema schema, List<String> values)
            throws BadRequestException, CharonException {
        return multi(schema, values, SCIMDefinitions.DataType.STRING);
    }

    public static MultiValuedAttribute multi(AttributeSchema schema, List<?> values, SCIMDefinitions.DataType innerType)
            throws CharonException, BadRequestException {
        if (!schema.getMultiValued() || schema.getType() != innerType) {
            throw new CharonException("Shall be called for multi-string attributes only");
        }
        MultiValuedAttribute attr = new MultiValuedAttribute(schema.getName());
        if (innerType == SCIMDefinitions.DataType.COMPLEX) {
            ArrayList<Attribute> attrs = new ArrayList<>();
            for (Object o: values) {
                attrs.add((Attribute) o);
            }
            attr.setAttributeValues(attrs);
        } else {
            attr.setAttributePrimitiveValues(new ArrayList<Object>(values));
        }
        return (MultiValuedAttribute) DefaultAttributeFactory.createAttribute(schema, attr);
    }

    public static MultiValuedAttribute ints(AttributeSchema schema, List<Integer> values)
            throws BadRequestException, CharonException {
        return multi(schema, values, SCIMDefinitions.DataType.INTEGER);
    }

    public static ComplexAttribute complex(AttributeSchema schema, List<Attribute> attributes)
            throws CharonException, BadRequestException {
        if (schema.getType() == SCIMDefinitions.DataType.COMPLEX) {
            ComplexAttribute attr = new ComplexAttribute(schema.getName());
            for (Attribute sub: attributes) {
                attr.setSubAttribute(sub);
            }
            return (ComplexAttribute) DefaultAttributeFactory.createAttribute(schema, attr);
        } else {
            throw new CharonException("shall be called for multi or complex attrs");
        }
    }

    public static String simpleString(AbstractSCIMObject obj, AttributeSchema schema) throws CharonException {
        SimpleAttribute attr = (SimpleAttribute) obj.getAttribute(schema.getName());
        return attr == null ? null : attr.getStringValue();
    }

    public static Integer simpleInt(AbstractSCIMObject obj, AttributeSchema schema) throws CharonException {
        SimpleAttribute attr = (SimpleAttribute) obj.getAttribute(schema.getName());
        return attr == null ? null : attr.getIntValue();
    }

    public static Boolean simpleBool(AbstractSCIMObject obj, AttributeSchema schema) throws CharonException {
        SimpleAttribute attr = (SimpleAttribute) obj.getAttribute(schema.getName());
        return attr == null ? null : attr.getBooleanValue();
    }

    public static Date simpleDate(AbstractSCIMObject obj, AttributeSchema schema) throws CharonException {
        SimpleAttribute attr = (SimpleAttribute) obj.getAttribute(schema.getName());
        return attr == null ? null : attr.getDateValue();
    }

    public static String simpleString(ComplexAttribute attribute, AttributeSchema schema) throws CharonException {
        SimpleAttribute attr = (SimpleAttribute) attribute.getSubAttribute(schema.getName());
        return attr == null ? null : attr.getStringValue();
    }

    public static Integer simpleInt(ComplexAttribute attribute, AttributeSchema schema) throws CharonException {
        SimpleAttribute attr = (SimpleAttribute) attribute.getSubAttribute(schema.getName());
        return attr == null ? null : attr.getIntValue();
    }

    public static Boolean simpleBool(ComplexAttribute attribute, AttributeSchema schema) throws CharonException {
        SimpleAttribute attr = (SimpleAttribute) attribute.getSubAttribute(schema.getName());
        return attr == null ? null : attr.getBooleanValue();
    }

    public static Date simpleDate(ComplexAttribute attribute, AttributeSchema schema) throws CharonException {
        SimpleAttribute attr = (SimpleAttribute) attribute.getSubAttribute(schema.getName());
        return attr == null ? null : attr.getDateValue();
    }

    public static ComplexAttribute complex(AbstractSCIMObject obj, AttributeSchema schema) {
        return (ComplexAttribute) obj.getAttribute(schema.getName());
    }

    public static <T> List<T> multiValues(AbstractSCIMObject obj, AttributeSchema schema) {
        return values(multi(obj, schema), SCIMConstants.CommonSchemaConstants.VALUE);
    }

    public static <T> List<T> values(List<ComplexAttribute> complex, String name) {
        List<T> out = new ArrayList<>(complex.size());
        for (ComplexAttribute ca: complex) {
            try {
                out.add((T) ((SimpleAttribute) ca.getSubAttribute(name)).getValue());
            } catch (CharonException e) {
                //impossible
                throw new RuntimeException(e);
            }
        }
        return out;
    }

    public static List<ComplexAttribute> multi(AbstractSCIMObject obj, AttributeSchema schema) {
        MultiValuedAttribute attr = (MultiValuedAttribute) obj.getAttribute(schema.getName());
        List<ComplexAttribute> out = new ArrayList<>();
        if (attr != null) {
            for (Object item: attr.getAttributePrimitiveValues()) {
                try {
                    ComplexAttribute inside = (ComplexAttribute) DefaultAttributeFactory.createAttribute(schema,
                            new ComplexAttribute(schema.getName()));
                    inside.setSubAttribute(DefaultAttributeFactory.createAttribute(
                            schema.getType() == SCIMDefinitions.DataType.COMPLEX
                                    ? schema.getSubAttributeSchema(SCIMConstants.CommonSchemaConstants.VALUE)
                                    : schema,
                            new SimpleAttribute(SCIMConstants.CommonSchemaConstants.VALUE, item)
                    ));
                    out.add(inside);
                } catch (BadRequestException e) {
                    throw new RuntimeException(e);
                }
            }
            for (Attribute item: attr.getAttributeValues()) {
                out.add((ComplexAttribute) item);
            }
        }
        return out;
    }


}
