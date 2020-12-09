# Storage

Activities and classes for a manager of eg files or bookmark entries.
Basically a recyclerview that operates on Strings as keys, allows
multi-selection and filtering.

* Version: 0.1.0
* gradle: `implementation project(':storage')`

## Technically

This project aims to solve its task as much the android way as possible. It involves

* SelectionTracker that uses String as keys, hence supporting frequent reordering of entries
* Theme-based colors for the selection
* Filtering entries in a RecyclerView using text input
    + Highlighting the matched parts in each entry
* Import/Export from/to zip-files
* Read from internal directory.
    
## Known bugs

* Change directory by using "/" in filename.
* Importing lots of files will cause a long stall
    + `Background concurrent copying GC freed 133452(6142KB) AllocSpace objects, 0(0B) LOS objects, 49% free, 10MB/21MB, paused 78us total 132.058ms`
