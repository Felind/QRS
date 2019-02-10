package com.felind.qrs;

public class Code {


        public Long _id; // for cupboard
        public String code; // bunny name
        public String type;


        public Code() {
            this.code = "noCode";
            this.type = "noType";
        }

        public Code(String code, String type) {
            this.code = code;
            this.type = type;
        }

}
