//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated from a template.
//
//     Manual changes to this file may cause unexpected behavior in your application.
//     Manual changes to this file will be overwritten if the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace WebApplication.Models.Database
{
    using System;
    using System.Collections.Generic;
    
    public partial class RowMorag3ahType
    {
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2214:DoNotCallOverridableMethodsInConstructors")]
        public RowMorag3ahType()
        {
            this.Morag3ah_DorrahShahed = new HashSet<Morag3ah_DorrahShahed>();
            this.Morag3ah_MawdeaKhlaf = new HashSet<Morag3ah_MawdeaKhlaf>();
            this.Morag3ah_MawdeaKhlafDetail = new HashSet<Morag3ah_MawdeaKhlafDetail>();
            this.Morag3ah_ShatibiyyahShahed = new HashSet<Morag3ah_ShatibiyyahShahed>();
        }
    
        public int ID { get; set; }
        public string Name { get; set; }
    
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2227:CollectionPropertiesShouldBeReadOnly")]
        public virtual ICollection<Morag3ah_DorrahShahed> Morag3ah_DorrahShahed { get; set; }
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2227:CollectionPropertiesShouldBeReadOnly")]
        public virtual ICollection<Morag3ah_MawdeaKhlaf> Morag3ah_MawdeaKhlaf { get; set; }
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2227:CollectionPropertiesShouldBeReadOnly")]
        public virtual ICollection<Morag3ah_MawdeaKhlafDetail> Morag3ah_MawdeaKhlafDetail { get; set; }
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2227:CollectionPropertiesShouldBeReadOnly")]
        public virtual ICollection<Morag3ah_ShatibiyyahShahed> Morag3ah_ShatibiyyahShahed { get; set; }
    }
}
