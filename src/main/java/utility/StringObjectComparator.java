package utility;

import java.io.Serializable;
import java.util.Comparator;

import entity.freebase.StringObject;

public class StringObjectComparator<K> implements Comparator<K>, Serializable{
     /**
      * Version id for serialization.
      */
     final static long serialVersionUID = 1L;


     /**
      * Compare two objects.
      *
      * @param obj1 First object
      * @param obj2 Second object
      * @return a positive integer if obj1 > obj2, 0 if obj1 == obj2,
      *         and a negative integer if obj1 < obj2
      */
      public int compare( Object obj1, Object obj2 )
      {
         if ( obj1 == null ) {
             throw new IllegalArgumentException( "Argument 'obj1' is null" );
         }

         if ( obj2 == null ) {
             throw new IllegalArgumentException( "Argument 'obj2' is null" );
         }

         return ( (StringObject) obj1 ).compareTo((StringObject) obj2 );
      }

 }
