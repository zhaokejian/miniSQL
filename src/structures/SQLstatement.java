package structures;

import java.util.ArrayList;

public class SQLstatement {
    STATEMENT_TYPE type;
    String tableName;
    String indexName;
    public ArrayList<Attribute> attributes = new ArrayList<>();
    ArrayList<Condition> conditions = new ArrayList<>();
    String content;

    public SQLstatement() {}

    public SQLstatement(String s) {
        String[] strArray = s.split(" ");
        switch (strArray[0].trim().charAt(0)) {
            case 'c':
                switch (strArray[1].trim().charAt(0)) {
                    case 't':
                        type = STATEMENT_TYPE.CREATE_TABLE;
                        tableName = strArray[2].trim();
                        content = s.substring(s.indexOf('(')+1, s.lastIndexOf((')'))).trim();
                        String[] strAttr = content.split(",");
                        for (int i=0; i<strAttr.length-1; i++) {
                            String[] strAttr2 = strAttr[i].trim().split(" ");
                            Attribute attribute = new Attribute();
                            attribute.setName(strAttr2[0].trim());
                            switch (strAttr2[1].trim().charAt(0)) {
                                case 'i':
                                    attribute.setType(DATA_TYPE.MYINT);
                                    attribute.setLength(4);
                                    break;
                                case 'f':
                                    attribute.setType(DATA_TYPE.MYFLOAT);
                                    attribute.setLength(4);
                                    break;
                                default:
                                    int charLength = Integer.parseInt(strAttr2[1].trim().substring(5, strAttr2[1].trim().indexOf(')')));
                                    attribute.setType(DATA_TYPE.MYCHAR);
                                    attribute.setLength(charLength);
                            }
                            if (strAttr2.length>2) {
                                if (strAttr2[2].equals("unique")) attribute.setIsUnique(true);
                                if (strAttr2[2].equals("primary")) attribute.setIsPrimKey(true);
                            }
                            attributes.add(attribute);
                        }
                        String strPrim = content.substring(content.indexOf("primary key") + 13, content.length()-1).trim();
                        for (int i=0; i<attributes.size(); i++)
                            if (attributes.get(i).getName().equals(strPrim)) {
                                attributes.get(i).setIsPrimKey(true);
                                break;
                            }
                        break;
                    case 'i':
                        type = STATEMENT_TYPE.CREATE_INDEX;
                        indexName = strArray[2].trim();
                        tableName = strArray[4].trim();
                        content = s.substring(s.indexOf('(')+1, s.indexOf(')')).trim();
                        break;
                }
                break;
            case 'd':
                if (strArray[0].equals("drop")) {
                    switch (strArray[1].trim().charAt(0)) {
                        case 't':
                            type = STATEMENT_TYPE.DROP_TABLE;
                            tableName = strArray[2];
                            break;
                        case 'i':
                            type = STATEMENT_TYPE.DROP_INDEX;
                            indexName = strArray[2];
                            break;
                        default:break;
                    }
                } else {
                    if (s.contains("where")) {
                        type = STATEMENT_TYPE.DELETE_WHERE;
                        String strCond = s.substring(s.indexOf("where")+6, s.length()-1).trim();
                        String[] strCondArr = strCond.split("and");
                        for (int i=0; i<strCondArr.length; i++) {
                            String[] strCondEle = strCondArr[i].split(" ");
                            Condition condition = new Condition();
                            condition.setAttrName(strCondEle[0].trim());
                            if (strCondEle[2].contains("\'")||strCondEle[2].contains("\"")) strCondEle[2] = strCondEle[2].trim().substring(1,strCondEle[2].length()-1);
                            condition.setValue(strCondEle[2]);
                            if (strCondEle[1].trim().equals(">")) condition.setRelationType(RELATION_TYPE.GREATER);
                            else if (strCondEle[1].trim().equals(">=")) condition.setRelationType(RELATION_TYPE.GREATER_EQUAL);
                            else if (strCondEle[1].trim().equals("<")) condition.setRelationType(RELATION_TYPE.SMALLER);
                            else if (strCondEle[1].trim().equals("<=")) condition.setRelationType(RELATION_TYPE.SMALLER_EQUAL);
                            else if (strCondEle[1].trim().equals("=")) condition.setRelationType(RELATION_TYPE.EQUAL);
                            else if (strCondEle[1].trim().equals("<>")) condition.setRelationType(RELATION_TYPE.NOT_EQUAL);
                            conditions.add(condition);
                        }
                    } else type = STATEMENT_TYPE.DELETE;
                    tableName = strArray[2].trim();
                }
                break;
            case 's':
                if (s.contains("where")) {
                    type = STATEMENT_TYPE.SELECT_WHERE;
                    String strCond = s.substring(s.indexOf("where")+6, s.length()-1).trim();
                    String[] strCondArr = strCond.split("and");
                    for (int i=0; i<strCondArr.length; i++) {
                        String[] strCondEle = strCondArr[i].split(" ");
                        Condition condition = new Condition();
                        condition.setAttrName(strCondEle[0].trim());
                        if (strCondEle[2].contains("\'")||strCondEle[2].contains("\"")) strCondEle[2] = strCondEle[2].trim().substring(1,strCondEle[2].length()-1);
                        condition.setValue(strCondEle[2]);
                        if (strCondEle[1].trim().equals(">")) condition.setRelationType(RELATION_TYPE.GREATER);
                        else if (strCondEle[1].trim().equals(">=")) condition.setRelationType(RELATION_TYPE.GREATER_EQUAL);
                        else if (strCondEle[1].trim().equals("<")) condition.setRelationType(RELATION_TYPE.SMALLER);
                        else if (strCondEle[1].trim().equals("<=")) condition.setRelationType(RELATION_TYPE.SMALLER_EQUAL);
                        else if (strCondEle[1].trim().equals("=")) condition.setRelationType(RELATION_TYPE.EQUAL);
                        else if (strCondEle[1].trim().equals("<>")) condition.setRelationType(RELATION_TYPE.NOT_EQUAL);

                    }
                } else type = STATEMENT_TYPE.SELECT;
                tableName = strArray[3].trim();
                tableName = tableName.substring(0,tableName.length()-1);
                String[] selectAttr = strArray[1].split(",");
                for (int i=0; i<selectAttr.length; i++) {
                    Attribute attribute = new Attribute();
                    attribute.setName(selectAttr[i].trim());
                    attributes.add(attribute);
                }
                break;
            case 'i':
                type = STATEMENT_TYPE.INSERT;
                tableName = strArray[2].trim();
                content = s.substring(s.indexOf('(')+1, s.indexOf(')')).trim();
                break;
            case 'q':
                type = STATEMENT_TYPE.QUIT;
            default:break;
        }
    }

    public void setStateType(STATEMENT_TYPE type) {
        this.type = type;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public ArrayList<Condition> getConditions() {
        return conditions;
    }

    public STATEMENT_TYPE getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void setConditions(ArrayList<Condition> conditions) {
        this.conditions = conditions;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
