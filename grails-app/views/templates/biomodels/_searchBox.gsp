 <form id="local-search" name="local-search" action="${createLink(controller: 'search', action: 'search')}" method="post">
                
          <fieldset>
          
          <div class="left">
            <label>
            <input type="text" value="${query}" name="search_block_form" id="local-searchbox"></input>
            </label>
          </div>
          
          <div class="right">
            <input type="submit" name="submit" value="Search" class="submit">          
            <!-- If your search is more complex than just a keyword search, you can link to an Advanced Search,
                 with whatever features you want available 
            <span class="adv"><a href="../search" id="adv-search" title="Advanced">Advanced</a></span>-->
          </div>                  
          
          </fieldset>
          
        </form>
