package com.grimni.domain;

import com.grimni.domain.ids.FileCourseBridgeId;
import jakarta.persistence.*;

@Entity
@Table(name = "file_course_bridge")
public class FileCourseBridge extends CreatedAtEntity {

    @EmbeddedId
    private FileCourseBridgeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fileId")
    @JoinColumn(name = "file_id")
    private FileObject file;
    
    public FileCourseBridge() {
    }

    public FileCourseBridgeId getId() {
        return id;
    }

    public void setId(FileCourseBridgeId id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public FileObject getFile() {
        return file;
    }

    public void setFile(FileObject file) {
        this.file = file;
    }

}