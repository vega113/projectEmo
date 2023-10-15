We need to allow deletion of a note. We will use a soft-delete, by marking isDeleted flag in DB as true. We will also allow to undo the deletion by displaying a message with a link to undo for 30 seconds.

So, the plan is:

1.  Add a new liquibase file to milestone 1 changes named accordingly under /Users/vega/devroot/projectEmo/conf/db/milestones. You can look at 📄  conf/db/changelog-create-tables.xml for example. Update 📄  conf/db/changelog-master.xml to refer to the new file
    
2.  create liquibase change to add deleted column to the notes table with default false.
    
3.  update the Note model in 📄  app/auth/model.scala to add the new column. also update Note parser in the same file
    
4.  Update the 📄  app/dao/NoteDao.scala so it will not fetch deleted notes.
    
5.  Add deleteNote/undeleteNote method to 📄  app/controllers/NoteController.scala and to 📄  app/service/NoteService.scala
    
6.  Update 📄  ui/src/app/display-emotion/display-emotion.component.html to add a delete button for a note (need to be a trash icon)
    
7.  update 📄  ui/src/app/display-emotion/display-emotion.component.ts to call service and update service so it will call endpoint to delete the note and to call an endpoint to undelete the note
    
8.  update 📄  ui/src/app/display-emotion/display-emotion.component.ts to show a message that note was deleted with a link to "undo". in case undo is clicked - call the service to undelete the note
    
9.  after delete/undelete we need to refresh the view