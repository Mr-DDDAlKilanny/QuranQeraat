using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using Microsoft.AspNet.Identity;
using Microsoft.AspNet.Identity.Owin;
using Microsoft.Owin.Security;
using WebApplication.Models.Database;
using System.Xml;
using System.Text;
using System.Runtime.CompilerServices;

namespace WebApplication.Controllers
{
    public class ServerController : Controller
    {
        private class MyJsonResult
        {
            public const int InvalidUsernamePassword = 401;
            public const int NotPermittedToSubmit = 403;
            public const int Ok = 200;
            public const int InternalServerError = 500;
            public MyJsonResult(string message, int code)
            {
                this.Message = message;
                this.Code = code;
            }
            public string Message { get; set; }
            public int Code { get; set; }
        }

        private class Detail
        {
            public int ID { get; set; }
            public int Owner { get; set; }
            public int Type { get; set; }
        }

        private class Shahed
        {
            public int Beit { get; set; }
            public byte Part { get; set; }
            public int Detail { get; set; }
            public bool Dorrah { get; set; }
        }

        private class Mawd3
        {
            public int ID { get; set; }
            public int Page { get; set; }
            public int Detail { get; set; }
            public float x1 { get; set; }
            public float x2 { get; set; }
            public float y1 { get; set; }
            public float y2 { get; set; }
        }

        private class Group
        {
            public int ID { get; set; }
            public int Detail { get; set; }
            public string Descr { get; set; }
        }

        private class Khlaf
        {
            public int Group { get; set; }
            public int Rewayah { get; set; }
            public bool Kholf { get; set; }
        }

        private void readXml(string xml, List<Detail> details, List<Shahed> shaheds, List<Mawd3> mawade3,
            List<Group> groups, List<Khlaf> khlafs)
        {
            XmlDocument doc = new XmlDocument();
            doc.LoadXml(xml);
            foreach (XmlNode item in doc.DocumentElement.GetElementsByTagName("MawdeaKhlafDetail").Item(0).ChildNodes)
            {
                details.Add(new Detail()
                {
                    ID = int.Parse(item.Attributes["_ID"].Value),
                    Owner = int.Parse(item.Attributes["OwnerMawdeaKhlaf"].Value),
                    Type = int.Parse(item.Attributes["KhlafType"].Value)
                });
            }
            foreach (XmlNode item in doc.DocumentElement.GetElementsByTagName("ShatibiyyahShahed").Item(0).ChildNodes)
            {
                shaheds.Add(new Shahed()
                {
                    Beit = int.Parse(item.Attributes["BeitID"].Value),
                    Detail = int.Parse(item.Attributes["DetailID"].Value),
                    Part = byte.Parse(item.Attributes["BeitPart"].Value),
                    Dorrah = false
                });
            }
            foreach (XmlNode item in doc.DocumentElement.GetElementsByTagName("DorrahShahed").Item(0).ChildNodes)
            {
                shaheds.Add(new Shahed()
                {
                    Beit = int.Parse(item.Attributes["BeitID"].Value),
                    Detail = int.Parse(item.Attributes["DetailID"].Value),
                    Part = byte.Parse(item.Attributes["BeitPart"].Value),
                    Dorrah = true
                });
            }
            foreach (XmlNode item in doc.DocumentElement.GetElementsByTagName("MawdeaKhlaf").Item(0).ChildNodes)
            {
                mawade3.Add(new Mawd3()
                {
                    ID = int.Parse(item.Attributes["_ID"].Value),
                    Page = int.Parse(item.Attributes["PageNumber"].Value),
                    Detail = int.Parse(item.Attributes["DetailID"].Value),
                    x1 = float.Parse(item.Attributes["x1"].Value),
                    y1 = float.Parse(item.Attributes["y1"].Value),
                    x2 = float.Parse(item.Attributes["x2"].Value),
                    y2 = float.Parse(item.Attributes["y2"].Value)
                });
            }
            foreach (XmlNode item in doc.DocumentElement.GetElementsByTagName("KhlafGroup").Item(0).ChildNodes)
            {
                groups.Add(new Group()
                {
                    ID = int.Parse(item.Attributes["_ID"].Value),
                    Detail = int.Parse(item.Attributes["MawdeaKhlafDetailID"].Value),
                    Descr = item.InnerText
                });
            }
            foreach (XmlNode item in doc.DocumentElement.GetElementsByTagName("Khlaf").Item(0).ChildNodes)
            {
                khlafs.Add(new Khlaf()
                {
                    Group = int.Parse(item.Attributes["KhlafGroupID"].Value),
                    Rewayah = int.Parse(item.Attributes["RewayahID"].Value),
                    Kholf = item.Attributes["HasKholf"].Value == "0"
                });
            }
        }
        
        [HttpPost]
        [MethodImpl(MethodImplOptions.Synchronized)]
        public string GetMorag3ahSelections(string username, string password)
        {
            var mgr = HttpContext.GetOwinContext().GetUserManager<ApplicationUserManager>();
            var usr = mgr.Find(username, password);
            if (usr == null)
                return MyJsonResult.InvalidUsernamePassword + ": Invalid username/password";
            using (var ent = new mushaf_qeraat_serverEntities())
            {
                var usrDb = ent.User.FirstOrDefault(k => k.UsersTableID == usr.Id);
                if (usrDb == null || usrDb.CanSubmit != true)
                    return MyJsonResult.NotPermittedToSubmit + ": Not permitted";
                StringBuilder b = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>").AppendLine("<all>");
                b.AppendLine("<DorrahShaheds>");
                foreach (var item in ent.DorrahShahed.Where(k => !k.IsDeleted))
                {
                    b.Append("<DorrahShahed DetailID=\"").Append(item.DetailID)
                        .Append("\" BeitID=\"").Append(item.BeitID)
                        .Append("\" BeitPart=\"").Append(item.BeitPart)
                        .AppendLine("\" />");
                }
                b.AppendLine("</DorrahShaheds>");

                b.AppendLine("<KhlafGroups>");
                foreach (var item in ent.KhlafGroup.Where(k => !k.IsDeleted))
                {
                    b.Append("<KhlafGroup _ID=\"").Append(item.C_ID)
                        .Append("\" MawdeaKhlafDetailID=\"").Append(item.MawdeaKhlafDetailID)
                        .Append("\">")
                        .Append(item.Description)
                        .AppendLine("</KhlafGroup>");
                }
                b.AppendLine("</KhlafGroups>");

                b.AppendLine("<Khlafs>");
                foreach (var item in ent.Khlaf.Where(k => !k.IsDeleted))
                {
                    b.Append("<Khlaf RewayahID=\"").Append(item.RewayahID)
                        .Append("\" HasKholf=\"").Append(item.HasKholf ? 1 : 0)
                        .Append("\" KhlafGroupID=\"").Append(item.KhlafGroupID)
                        .AppendLine("\" />");
                }
                b.AppendLine("</Khlafs>");

                b.AppendLine("<MawdeaKhlafDetails>");
                foreach (var item in ent.MawdeaKhlafDetail.Where(k => !k.IsDeleted))
                {
                    b.Append("<MawdeaKhlafDetail _ID=\"").Append(item.C_ID)
                        .Append("\" OwnerMawdeaKhlaf=\"").Append(item.OwnerMawdeaKhlaf)
                        .Append("\" KhlafType=\"").Append(item.KhlafType)
                        .AppendLine("\" />");
                }
                b.AppendLine("</MawdeaKhlafDetails>");

                b.AppendLine("<MawdeaKhlafs>");
                foreach (var item in ent.MawdeaKhlaf.Where(k => !k.IsDeleted))
                {
                    b.Append("<MawdeaKhlaf _ID=\"").Append(item.C_ID)
                        .Append("\" PageNumber=\"").Append(item.PageNumber)
                        .Append("\" DetailID=\"").Append(item.DetailID)
                        .Append("\" x1=\"").Append(item.x1)
                        .Append("\" y1=\"").Append(item.y1)
                        .Append("\" x2=\"").Append(item.x2)
                        .Append("\" y2=\"").Append(item.y2)
                        .AppendLine("\" />");
                }
                b.AppendLine("</MawdeaKhlafs>");

                b.AppendLine("<ShatibiyyahShaheds>");
                foreach (var item in ent.DorrahShahed.Where(k => !k.IsDeleted))
                {
                    b.Append("<ShatibiyyahShahed DetailID=\"").Append(item.DetailID)
                        .Append("\" BeitID=\"").Append(item.BeitID)
                        .Append("\" BeitPart=\"").Append(item.BeitPart)
                        .AppendLine("\" />");
                }
                b.AppendLine("</ShatibiyyahShaheds>");
                return MyJsonResult.Ok + ": " + b.Append("</all>");
            }
        }

        [HttpPost]
        [MethodImpl(MethodImplOptions.Synchronized)]
        [ValidateInput(false)]
        public JsonResult SubmitMorag3ahSelections(string username, string password, string xmlData)
        {
            return null;
        }

        [HttpPost]
        [MethodImpl(MethodImplOptions.Synchronized)]
        [ValidateInput(false)]
        public JsonResult SubmitUserSelections(string username, string password, string xmlData)
        {
            var mgr = HttpContext.GetOwinContext().GetUserManager<ApplicationUserManager>();
            var usr = mgr.Find(username, password);
            if (usr == null)
                return Json(new MyJsonResult("Invalid username/password", MyJsonResult.InvalidUsernamePassword));
            using (var ent = new mushaf_qeraat_serverEntities())
            {
                var usrDb = ent.User.FirstOrDefault(k => k.UsersTableID == usr.Id);
                if (usrDb == null || usrDb.CanSubmit != true)
                    return Json(new MyJsonResult("Not permitted to submit", MyJsonResult.NotPermittedToSubmit));

                var details = new List<Detail>();
                var shaheds = new List<Shahed>();
                var mawade3 = new List<Mawd3>();
                var groups = new List<Group>();
                var khlafs = new List<Khlaf>();
                readXml(xmlData, details, shaheds, mawade3, groups, khlafs);

                using (var trans = ent.Database.BeginTransaction())
                {
                    try
                    {
                        var mySubmits = ent.UserSubmit.Where(k => k.UserId == usrDb.ID);
                        var myShatibiyah = ent.Submit_ShatibiyyahShahed.Where(m => mySubmits.Any(k => k.ID == m.SubmitID));
                        var myDorrah = ent.Submit_DorrahShahed.Where(m => mySubmits.Any(k => k.ID == m.SubmitID));
                        var myDetails = ent.Submit_MawdeaKhlafDetail.Where(m => mySubmits.Any(k => k.ID == m.SubmitID));
                        var myMawadea = ent.Submit_MawdeaKhlaf.Where(m => mySubmits.Any(k => k.ID == m.SubmitID));
                        var myGroups = ent.Submit_KhlafGroup.Where(m => mySubmits.Any(k => k.ID == m.SubmitID));
                        var myKhlafs = ent.Submit_Khlaf.Where(m => mySubmits.Any(k => k.ID == m.SubmitID));
                        var submit = new UserSubmit()
                        {
                            UserId = usrDb.ID,
                            SubmitTime = DateTime.Now
                        };
                        ent.UserSubmit.Add(submit);
                        ent.SaveChanges();
                        var resolve = new List<KeyValuePair<MawdeaKhlafDetail, Detail>>();
                        #region Detail
                        var det_i = from b in details
                                    where !myDetails.Any(k => k.TableID == b.ID)
                                    select b;
                        foreach (var item in det_i)
                        {
                            var id = myMawadea.FirstOrDefault(k => k.TableID == item.Owner);
                            var obj = new MawdeaKhlafDetail()
                            {
                                KhlafType = item.Type
                            };
                            if (id == null)
                                resolve.Add(new KeyValuePair<MawdeaKhlafDetail, Detail>(obj, item));
                            else obj.OwnerMawdeaKhlaf = id.InsertedID;
                            ent.MawdeaKhlafDetail.Add(obj);
                            ent.SaveChanges();
                            ent.Submit_MawdeaKhlafDetail.Add(new Submit_MawdeaKhlafDetail()
                            {
                                DetailType = item.Type,
                                OwnerMawdeaKhlaf = item.Owner,
                                SubmitID = submit.ID,
                                TableID = item.ID,
                                InsertedID = obj.C_ID
                            });
                        }
                        var det_u = from b in details
                                    where myDetails.Any(k => k.TableID == b.ID)
                                    select b;
                        foreach (var item in det_u)
                        {
                            var id = myDetails.First(k => k.TableID == item.ID).InsertedID;
                            var up = ent.MawdeaKhlafDetail.First(k => k.C_ID == id);
                            var owner = myMawadea.FirstOrDefault(k => k.TableID == item.Owner);
                            if (owner == null)
                                resolve.Add(new KeyValuePair<MawdeaKhlafDetail, Detail>(up, item));
                            else up.OwnerMawdeaKhlaf = owner.InsertedID;
                            ent.Submit_MawdeaKhlafDetail.Add(new Submit_MawdeaKhlafDetail()
                            {
                                DetailType = item.Type,
                                OwnerMawdeaKhlaf = item.Owner,
                                SubmitID = submit.ID,
                                TableID = item.ID,
                                InsertedID = id,
                                RejectReason = 1
                            });
                        }
                        var ints = (from b in details
                                    select b.ID).ToArray();
                        var det_d = from b in myDetails
                                    where !ints.Any(k => k == b.TableID)
                                    select b;
                        foreach (var item in det_d)
                        {
                            var del = ent.MawdeaKhlafDetail.First(k => k.C_ID == item.InsertedID);
                            if (!del.IsDeleted)
                            {
                                del.IsDeleted = true;
                                ent.Submit_MawdeaKhlafDetail.Add(new Submit_MawdeaKhlafDetail()
                                {
                                    DetailType = del.KhlafType,
                                    OwnerMawdeaKhlaf = del.OwnerMawdeaKhlaf,
                                    InsertedID = item.InsertedID,
                                    RejectReason = 2,
                                    SubmitID = submit.ID
                                });
                            }
                        }
                        #endregion
                        #region Mawdea Khlaf
                        var mawd_i = from b in mawade3
                                     where !myMawadea.Any(k => k.TableID == b.ID)
                                     select b;
                        foreach (var item in mawd_i)
                        {
                            var obj = new MawdeaKhlaf()
                            {
                                DetailID = (int)myDetails.First(k => k.TableID == item.Detail).InsertedID,
                                PageNumber = item.Page,
                                x1 = item.x1, y1 = item.y1, x2 = item.x2, y2 = item.y2
                            };
                            ent.MawdeaKhlaf.Add(obj);
                            ent.SaveChanges();
                            ent.Submit_MawdeaKhlaf.Add(new Submit_MawdeaKhlaf()
                            {
                                TableID = item.ID,
                                PageNumber = item.Page,
                                x1 = item.x1,
                                y1 = item.y1,
                                x2 = item.x2,
                                y2 = item.y2,
                                InsertedID = obj.C_ID, 
                                DetailID = item.Detail, 
                                SubmitID = submit.ID
                            });
                        }
                        var mawd_u = from b in mawade3
                                     where myMawadea.Any(k => k.TableID == b.ID)
                                     select b;
                        foreach (var item in mawd_u)
                        {
                            var id = myMawadea.First(k => k.TableID == item.ID).InsertedID;
                            var up = ent.MawdeaKhlaf.First(k => k.C_ID == id);
                            up.x1 = item.x1; up.x2 = item.x2; up.y1 = item.y1; up.y2 = item.y2;
                            ent.Submit_MawdeaKhlaf.Add(new Submit_MawdeaKhlaf()
                            {
                                TableID = item.ID,
                                PageNumber = item.Page,
                                x1 = item.x1,
                                y1 = item.y1,
                                x2 = item.x2,
                                y2 = item.y2,
                                InsertedID = id,
                                RejectReason = 1,
                                DetailID = item.Detail,
                                SubmitID = submit.ID
                            });
                        }
                        ints = (from b in mawade3 select b.ID).ToArray();
                        var mawd_d = from b in myMawadea
                                     where !ints.Any(k => k == b.TableID)
                                     select b;
                        foreach (var item in mawd_d)
                        {
                            var del = ent.MawdeaKhlaf.First(k => k.C_ID == item.InsertedID);
                            if (!del.IsDeleted)
                            {
                                del.IsDeleted = true;
                            
                                ent.Submit_MawdeaKhlaf.Add(new Submit_MawdeaKhlaf()
                                {
                                    PageNumber = del.PageNumber,
                                    x1 = del.x1,
                                    y1 = del.y1,
                                    x2 = del.x2,
                                    y2 = del.y2,
                                    InsertedID = del.C_ID,
                                    RejectReason = 2,
                                    DetailID = del.DetailID,
                                    SubmitID = submit.ID
                                });
                            }
                        }
                        #endregion
                        ent.SaveChanges();
                        foreach (var item in resolve)
                            item.Key.OwnerMawdeaKhlaf = myMawadea.First(k => k.TableID == item.Value.Owner).InsertedID;

                        #region Shahed
                        var shat_i = from b in shaheds
                                     where !b.Dorrah && !myShatibiyah.Any(k => k.TableDetailID == b.Detail && k.BeitID == b.Beit)
                                     select b;
                        foreach (var item in shat_i)
                        {
                            var obj = new ShatibiyyahShahed()
                            {
                                BeitID = item.Beit,
                                BeitPart = item.Part,
                                DetailID = (int)myDetails.First(k => k.TableID == item.Detail).InsertedID
                            };
                            ent.ShatibiyyahShahed.Add(obj);
                            ent.SaveChanges();
                            ent.Submit_ShatibiyyahShahed.Add(new Submit_ShatibiyyahShahed()
                            {
                                BeitID = item.Beit,
                                BeitPart = item.Part,
                                SubmitID = submit.ID,
                                TableDetailID = item.Detail,
                                InsertedDetailID = obj.DetailID
                            });
                        } 
                        var shat_tmp = (from b in myShatibiyah
                                          select new { b.TableDetailID, b.BeitID }).ToArray();
                        var shat_d = from b in shat_tmp
                                     where !shaheds.Any(k => !k.Dorrah && b.TableDetailID == k.Detail && b.BeitID == k.Beit)
                                     select b;
                        foreach (var i in shat_d)
                        {
                            var item = myShatibiyah.First(k => k.BeitID == i.BeitID && k.TableDetailID == i.TableDetailID);
                            var del = ent.ShatibiyyahShahed.First(k => k.DetailID == item.InsertedDetailID && k.BeitID == item.BeitID);
                            if (!del.IsDeleted)
                            {
                                del.IsDeleted = true;
                                ent.Submit_ShatibiyyahShahed.Add(new Submit_ShatibiyyahShahed()
                                {
                                    BeitID = item.BeitID,
                                    BeitPart = item.BeitPart,
                                    SubmitID = submit.ID,
                                    InsertedDetailID = item.InsertedDetailID,
                                    RejectReason = 2
                                });
                            }
                        }

                        var dor_i = from b in shaheds
                                    where b.Dorrah && !myDorrah.Any(k => k.TableDetailID == b.Detail && k.BeitID == b.Beit)
                                    select b;
                        foreach (var item in dor_i)
                        {
                            var obj = new DorrahShahed()
                            {
                                BeitID = item.Beit,
                                BeitPart = item.Part,
                                DetailID = (int)myDetails.First(k => k.TableID == item.Detail).InsertedID
                            };
                            ent.DorrahShahed.Add(obj);
                            ent.SaveChanges();
                            ent.Submit_DorrahShahed.Add(new Submit_DorrahShahed()
                            {
                                BeitID = item.Beit,
                                BeitPart = item.Part,
                                SubmitID = submit.ID,
                                TableDetailID = item.Detail,
                                InsertedDetailID = obj.DetailID
                            });
                        } 
                        var dor_tmp = (from b in myDorrah
                                          select new { b.TableDetailID, b.BeitID }).ToArray();
                        var dor_d = from b in dor_tmp
                                     where !shaheds.Any(k => k.Dorrah && b.TableDetailID == k.Detail && b.BeitID == k.Beit)
                                     select b;
                        foreach (var i in dor_d)
                        {
                            var item = myDorrah.First(k => k.BeitID == i.BeitID && k.TableDetailID == i.TableDetailID);
                            var del = ent.DorrahShahed.First(k => k.DetailID == item.InsertedDetailID && k.BeitID == item.BeitID);
                            if (!del.IsDeleted)
                            {
                                del.IsDeleted = true;
                                ent.Submit_DorrahShahed.Add(new Submit_DorrahShahed()
                                {
                                    BeitID = item.BeitID,
                                    BeitPart = item.BeitPart,
                                    SubmitID = submit.ID,
                                    InsertedDetailID = item.InsertedDetailID,
                                    RejectReason = 2
                                });
                            }
                        }
                        #endregion
                        #region Group
                        var gr_i = from b in groups
                                   where !myGroups.Any(k => k.TableID == b.ID)
                                   select b;
                        foreach (var item in gr_i)
                        {
                            var obj = new KhlafGroup()
                            {
                                Description = item.Descr,
                                MawdeaKhlafDetailID = (int)myDetails.First(k => k.TableID == item.Detail).InsertedID
                            };
                            ent.KhlafGroup.Add(obj);
                            ent.SaveChanges();
                            ent.Submit_KhlafGroup.Add(new Submit_KhlafGroup()
                            {
                                Description = item.Descr,
                                InsertedID = obj.C_ID,
                                SubmitID = submit.ID,
                                TableID = item.ID,
                                MawdeaKhlafDetailID = item.Detail
                            });
                        }
                        var gr_u = from b in groups
                                   where myGroups.Any(k => k.TableID == b.ID)
                                   select b;
                        foreach (var item in gr_u)
                        {
                            var id = myGroups.First(k => k.TableID == item.ID).InsertedID;
                            var up = ent.KhlafGroup.First(k => k.C_ID == id);
                            up.Description = item.Descr;
                            ent.Submit_KhlafGroup.Add(new Submit_KhlafGroup()
                            {
                                Description = item.Descr,
                                InsertedID = id, RejectReason = 1,
                                SubmitID = submit.ID,
                                TableID = item.ID,
                                MawdeaKhlafDetailID = item.Detail
                            });
                        }
                        ints = (from b in groups select b.ID).ToArray();
                        var gr_d = from b in myGroups
                                   where !ints.Any(k => k == b.TableID)
                                   select b;
                        foreach (var item in gr_d)
                        {
                            var del = ent.KhlafGroup.First(k => k.C_ID == item.InsertedID);
                            if (!del.IsDeleted)
                            {
                                del.IsDeleted = true;
                                ent.Submit_KhlafGroup.Add(new Submit_KhlafGroup()
                                {
                                    Description = item.Description,
                                    InsertedID = item.InsertedID,
                                    RejectReason = 2,
                                    SubmitID = submit.ID,
                                    MawdeaKhlafDetailID = item.MawdeaKhlafDetailID
                                });
                            }
                        }
                        #endregion
                        #region Khlaf
                        var kh_i = from b in khlafs
                                   where !myKhlafs.Any(k => k.TableKhlafGroupID == b.Group && k.TableRewayahID == b.Rewayah)
                                   select b;
                        foreach (var item in kh_i)
                        {
                            var obj = new WebApplication.Models.Database.Khlaf()
                            {
                                HasKholf = item.Kholf,
                                KhlafGroupID = (int)myGroups.First(k => k.TableID == item.Group).InsertedID,
                                RewayahID = item.Rewayah
                            };
                            ent.Khlaf.Add(obj);
                            ent.Submit_Khlaf.Add(new Submit_Khlaf()
                            {
                                InsertedKhlafGroupID = obj.KhlafGroupID,
                                InsertedRewayahID = item.Rewayah,
                                TableKhlafGroupID = item.Group,
                                TableRewayahID = item.Rewayah,
                                SubmitID = submit.ID,
                                HasKholf = item.Kholf
                            });
                        }
                        var kh_u = from b in khlafs
                                   where myKhlafs.Any(k => k.TableKhlafGroupID == b.Group && k.TableRewayahID == b.Rewayah)
                                   select b;
                        foreach (var item in kh_u)
                        {
                            var id = myKhlafs.First(k => k.TableKhlafGroupID == item.Group && k.TableRewayahID == item.Rewayah)
                                .InsertedKhlafGroupID;
                            var up = ent.Khlaf.First(k => k.KhlafGroupID == id && k.RewayahID == item.Rewayah);
                            up.HasKholf = item.Kholf;
                            ent.Submit_Khlaf.Add(new Submit_Khlaf()
                            {
                                InsertedKhlafGroupID = id,
                                InsertedRewayahID = item.Rewayah,
                                TableKhlafGroupID = item.Group,
                                TableRewayahID = item.Rewayah,
                                SubmitID = submit.ID,
                                HasKholf = item.Kholf, RejectReason = 1
                            });
                        }
                        var kh_tmp = (from b in myKhlafs select new { b.TableKhlafGroupID, b.TableRewayahID }).ToArray();
                        var kh_d = from b in kh_tmp
                                   where !khlafs.Any(k => b.TableKhlafGroupID == k.Group && b.TableRewayahID == k.Rewayah)
                                   select b;
                        foreach (var i in kh_d)
                        {
                            var item = myKhlafs.First(k => k.TableRewayahID == i.TableRewayahID && k.TableKhlafGroupID == i.TableKhlafGroupID);
                            var del = ent.Khlaf.First(k => k.KhlafGroupID == item.InsertedKhlafGroupID && k.RewayahID == item.InsertedRewayahID);
                            if (!del.IsDeleted)
                            {
                                del.IsDeleted = true;
                                ent.Submit_Khlaf.Add(new Submit_Khlaf()
                                {
                                    InsertedKhlafGroupID = item.InsertedKhlafGroupID,
                                    InsertedRewayahID = item.InsertedRewayahID,
                                    SubmitID = submit.ID,
                                    HasKholf = item.HasKholf,
                                    RejectReason = 2
                                });
                            }
                        }
                        #endregion
                        ent.SaveChanges();
                        trans.Commit();
                    }
                    catch (Exception ex)
                    {
                        string message = ex.Message;
                        while (ex.InnerException != null)
                        {
                            ex = ex.InnerException;
                            message = ex.Message;
                        }
                        return Json(new MyJsonResult("Internal Server Error\n" + message, MyJsonResult.InternalServerError));
                    }
                }
            }
            return Json(new MyJsonResult("Ok", MyJsonResult.Ok));
        }
    }
}