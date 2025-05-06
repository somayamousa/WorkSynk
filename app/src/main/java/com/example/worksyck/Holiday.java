package com.example.worksyck;

public class Holiday {

        int id;
        String name, date, description;

        public Holiday(int id,String name, String date, String description) {
            this.id=id;
            this.name = name;
            this.date = date;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setDescription(String description) {
            this.description = description;
        }


}
